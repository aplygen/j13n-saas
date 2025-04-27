package io.j13n.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
public class VirtualThreadManager {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadManager.class);

    private final ExecutorService virtualThreadExecutor;
    private final ConcurrentHashMap<String, Thread> activeThreads = new ConcurrentHashMap<>();
    private final AtomicInteger totalThreadsCreated = new AtomicInteger(0);
    private final AtomicInteger activeThreadCount = new AtomicInteger(0);
    private final AtomicInteger completedThreadCount = new AtomicInteger(0);
    private final AtomicInteger failedThreadCount = new AtomicInteger(0);

    public VirtualThreadManager() {
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        logger.info("VirtualThreadManager initialized with virtual thread executor");
    }

    public CompletableFuture<Void> submitTask(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        virtualThreadExecutor.submit(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <T> CompletableFuture<T> submitTask(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        virtualThreadExecutor.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <T> CompletableFuture<T> submitTask(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        virtualThreadExecutor.submit(() -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <T> List<CompletableFuture<T>> executeAll(Collection<Callable<T>> tasks) {
        return tasks.stream()
                .map(this::submitTask)
                .toList();
    }

    public <T> CompletableFuture<T> executeAny(Collection<Callable<T>> tasks) {
        CompletableFuture<T> result = new CompletableFuture<>();

        // Submit all tasks
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submitTask)
                .toList();

        // Complete the result future when any of the futures completes
        CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    for (CompletableFuture<T> future : futures) {
                        if (future.isDone() && !future.isCompletedExceptionally()) {
                            try {
                                result.complete(future.get());
                                // Cancel all other futures
                                futures.forEach(f -> {
                                    if (f != future) {
                                        f.cancel(true);
                                    }
                                });
                                return;
                            } catch (Exception e) {
                                // Continue to next future
                            }
                        }
                    }
                    // If we get here, all futures completed exceptionally
                    result.completeExceptionally(new ExecutionException("All tasks failed", null));
                });

        return result;
    }

    public Thread newVirtualThread(String name, Runnable task) {
        totalThreadsCreated.incrementAndGet();
        activeThreadCount.incrementAndGet();

        Runnable wrappedTask = () -> {
            try {
                task.run();
                completedThreadCount.incrementAndGet();
            } catch (Exception e) {
                failedThreadCount.incrementAndGet();
                logger.error("Virtual thread execution failed: {}", e.getMessage(), e);
                throw e;
            } finally {
                activeThreadCount.decrementAndGet();
                activeThreads.remove(name);
            }
        };

        Thread thread = Thread.ofVirtual().name(name).start(wrappedTask);
        activeThreads.put(name, thread);
        return thread;
    }

    public Thread newVirtualThread(Runnable task) {
        String name = "virtual-thread-" + totalThreadsCreated.get();
        return newVirtualThread(name, task);
    }

    public Thread.Builder.OfVirtual virtualThreadBuilder() {
        return Thread.ofVirtual();
    }

    public <T> List<T> executeAllAndGet(Collection<Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submitTask)
                .toList();

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Task execution was interrupted", e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Task execution failed", e);
                    }
                })
                .toList();
    }

    public <T> List<T> executeAllAndGet(Collection<Callable<T>> tasks, Duration timeout) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submitTask)
                .toList();

        // Create a timeout for all futures
        CompletableFuture<Void> timeoutFuture = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> timeoutFuture.complete(null), timeout.toMillis(), TimeUnit.MILLISECONDS);

        // Apply timeout to all futures
        List<CompletableFuture<T>> timedFutures = futures.stream()
                .map(future -> {
                    CompletableFuture<T> timedFuture = new CompletableFuture<>();
                    future.thenAccept(timedFuture::complete);
                    timeoutFuture.thenRun(() -> {
                        if (!future.isDone()) {
                            future.cancel(true);
                        }
                    });
                    return timedFuture;
                })
                .toList();

        // Wait for all futures to complete or timeout
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .exceptionally(e -> null)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Task execution was interrupted", e);
        } catch (ExecutionException e) {
            // Some tasks failed, but we'll still return the successful ones
        } catch (TimeoutException e) {
            // Timeout occurred, we'll return the completed tasks
        } finally {
            scheduler.shutdown();
        }

        return futures.stream()
                .filter(CompletableFuture::isDone)
                .filter(future -> !future.isCompletedExceptionally())
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Task execution was interrupted", e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Task execution failed", e);
                    }
                })
                .toList();
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, Duration delay) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        return scheduler.schedule(
                () -> {
                    try {
                        virtualThreadExecutor.submit(task);
                    } finally {
                        scheduler.shutdown();
                    }
                },
                delay.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public <T> ScheduledFuture<T> scheduleTask(Callable<T> task, Duration delay) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<Future<T>> scheduledFuture = scheduler.schedule(
                () -> {
                    try {
                        return virtualThreadExecutor.submit(task);
                    } finally {
                        scheduler.shutdown();
                    }
                },
                delay.toMillis(),
                TimeUnit.MILLISECONDS);

        return new ScheduledFuture<T>() {
            @Override
            public long getDelay(TimeUnit unit) {
                return scheduledFuture.getDelay(unit);
            }

            @Override
            public int compareTo(Delayed o) {
                return scheduledFuture.compareTo(o);
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return scheduledFuture.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return scheduledFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return scheduledFuture.isDone();
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                return scheduledFuture.get().get();
            }

            @Override
            public T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                Future<T> future = scheduledFuture.get(timeout, unit);
                return future.get(timeout, unit);
            }
        };
    }

    public Map<String, Integer> getThreadStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("totalThreadsCreated", totalThreadsCreated.get());
        stats.put("activeThreadCount", activeThreadCount.get());
        stats.put("completedThreadCount", completedThreadCount.get());
        stats.put("failedThreadCount", failedThreadCount.get());
        return stats;
    }

    public Set<String> getActiveThreadNames() {
        return activeThreads.keySet();
    }

    public Map<String, Thread> getActiveThreads() {
        return new ConcurrentHashMap<>(activeThreads);
    }

    public boolean interruptThread(String threadName) {
        Thread thread = activeThreads.get(threadName);
        if (thread != null) {
            thread.interrupt();
            return true;
        }
        return false;
    }

    public void interruptAllThreads() {
        activeThreads.values().forEach(Thread::interrupt);
    }

    public void shutdown() {
        virtualThreadExecutor.shutdown();
        logger.info("Virtual thread executor shutdown initiated");
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> pendingTasks = virtualThreadExecutor.shutdownNow();
        logger.info("Virtual thread executor shutdown forced, {} pending tasks cancelled", pendingTasks.size());
        return pendingTasks;
    }

    public boolean awaitTermination(Duration timeout) {
        try {
            boolean terminated = virtualThreadExecutor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (terminated) {
                logger.info("Virtual thread executor terminated successfully");
            } else {
                logger.warn("Virtual thread executor did not terminate within the specified timeout");
            }
            return terminated;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Awaiting termination of virtual thread executor was interrupted");
            return false;
        }
    }

    public boolean isShutdown() {
        return virtualThreadExecutor.isShutdown();
    }

    public boolean isTerminated() {
        return virtualThreadExecutor.isTerminated();
    }

    public boolean gracefulShutdown(Duration timeout) {
        shutdown();
        boolean terminated = false;
        try {
            terminated = virtualThreadExecutor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!terminated) {
                List<Runnable> pendingTasks = shutdownNow();
                logger.warn("Graceful shutdown timed out, forced shutdown with {} pending tasks", pendingTasks.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            shutdownNow();
            logger.warn("Graceful shutdown was interrupted, forced shutdown initiated");
        }
        return terminated;
    }
}
