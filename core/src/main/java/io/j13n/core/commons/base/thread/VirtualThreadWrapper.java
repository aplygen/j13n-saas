package io.j13n.core.commons.base.thread;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrapper class to help convert reactive code to use virtual threads.
 * This class provides methods that mimic reactive programming patterns using virtual threads.
 */
public class VirtualThreadWrapper {

    /**
     * Wraps a value in a CompletableFuture, similar to Mono.just().
     *
     * @param value The value to wrap
     * @param <T> The type of the value
     * @return A CompletableFuture containing the value
     */
    public static <T> CompletableFuture<T> just(T value) {
        return VirtualThreadExecutor.just(value);
    }

    /**
     * Executes a task in a virtual thread, similar to Mono.fromCallable().
     *
     * @param supplier The task to execute
     * @param <T> The type of the result
     * @return A CompletableFuture containing the result
     */
    public static <T> CompletableFuture<T> fromCallable(Supplier<T> supplier) {
        return VirtualThreadExecutor.async(supplier);
    }

    /**
     * Transforms a CompletableFuture using a function, similar to Mono.flatMap().
     *
     * @param future The CompletableFuture to transform
     * @param mapper The function to apply
     * @param <T> The input type
     * @param <R> The output type
     * @return A CompletableFuture containing the transformed result
     */
    public static <T, R> CompletableFuture<R> flatMap(CompletableFuture<T> future, Function<T, CompletableFuture<R>> mapper) {
        return future.thenCompose(mapper);
    }

    /**
     * Transforms a CompletableFuture using a function, similar to Mono.map().
     *
     * @param future The CompletableFuture to transform
     * @param mapper The function to apply
     * @param <T> The input type
     * @param <R> The output type
     * @return A CompletableFuture containing the transformed result
     */
    public static <T, R> CompletableFuture<R> map(CompletableFuture<T> future, Function<T, R> mapper) {
        return future.thenApply(mapper);
    }

    /**
     * Executes multiple tasks in parallel and combines their results, similar to Flux.fromIterable().
     *
     * @param tasks The list of tasks to execute
     * @param <T> The type of the results
     * @return A CompletableFuture containing a list of results
     */
    public static <T> CompletableFuture<List<T>> fromIterable(List<Supplier<T>> tasks) {
        return VirtualThreadExecutor.all(tasks);
    }

    /**
     * Handles errors in a CompletableFuture, similar to Mono.onErrorResume().
     *
     * @param future The CompletableFuture to handle
     * @param errorHandler The function to handle errors
     * @param <T> The type of the result
     * @return A CompletableFuture containing the result or the error handler's result
     */
    public static <T> CompletableFuture<T> onErrorResume(CompletableFuture<T> future,
                                                       Function<Throwable, CompletableFuture<T>> errorHandler) {
        return future.handle((result, throwable) -> {
            if (throwable != null) {
                return errorHandler.apply(throwable);
            }
            return CompletableFuture.completedFuture(result);
        }).thenCompose(Function.identity());
    }

    /**
     * Provides a default value if a CompletableFuture completes with an error, similar to Mono.onErrorReturn().
     *
     * @param future The CompletableFuture to handle
     * @param defaultValue The default value to return on error
     * @param <T> The type of the result
     * @return A CompletableFuture containing the result or the default value
     */
    public static <T> CompletableFuture<T> onErrorReturn(CompletableFuture<T> future, T defaultValue) {
        return future.exceptionally(throwable -> defaultValue);
    }

    /**
     * Delays the execution of a CompletableFuture, similar to Mono.delay().
     *
     * @param delay The delay in milliseconds
     * @return A CompletableFuture that completes after the delay
     */
    public static CompletableFuture<Void> delay(long delay) {
        return VirtualThreadExecutor.delay(delay);
    }
}