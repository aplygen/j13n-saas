package io.j13n.core.service.scrape;

import io.j13n.core.model.scrape.JobScrapingResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class JSoupScrapingService extends AbstractScrapingService {

    private static final Logger logger = LoggerFactory.getLogger(JSoupScrapingService.class);
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    // Connection pool using ConcurrentHashMap to store pre-configured connections
    private final ConcurrentHashMap<String, Document.OutputSettings> connectionPool = new ConcurrentHashMap<>();

    // Semaphore for limiting concurrent scraping
    private final Semaphore scrapingSemaphore;

    @Value("${scraping.timeout:10000}")
    private int timeoutMillis;

    @Value("${scraping.max-concurrent:10}")
    private int maxConcurrentScrapes;

    @Value("${scraping.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36}")
    private String userAgent;

    @Value("${scraping.proxy.host:#{null}}")
    private String proxyHost;

    @Value("${scraping.proxy.port:0}")
    private int proxyPort;

    public JSoupScrapingService() {
        this.scrapingSemaphore = new Semaphore(maxConcurrentScrapes, true);
    }

    @Override
    @Retryable(
        value = { IOException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<JobScrapingResult> scrapeJobDetails(String url) {
        if (!supportsUrl(url)) {
            return CompletableFuture.completedFuture(
                new JobScrapingResult()
                    .setSourceUrl(url)
                    .setSuccessful(false)
                    .setErrorMessage("Invalid URL format")
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Acquire semaphore permit with timeout
                if (!scrapingSemaphore.tryAcquire(30, TimeUnit.SECONDS)) {
                    return new JobScrapingResult()
                        .setSourceUrl(url)
                        .setSuccessful(false)
                        .setErrorMessage("Too many concurrent scraping requests");
                }

                return scrapeWithRetry(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new JobScrapingResult()
                    .setSourceUrl(url)
                    .setSuccessful(false)
                    .setErrorMessage("Scraping interrupted: " + e.getMessage());
            } finally {
                scrapingSemaphore.release();
            }
        });
    }

    private JobScrapingResult scrapeWithRetry(String url) {
        JobScrapingResult result = new JobScrapingResult()
            .setSourceUrl(url)
            .setSuccessful(true);

        try {
            // Get or create connection settings
            Document.OutputSettings settings = connectionPool.computeIfAbsent(
                getDomain(url),
                domain -> createConnectionSettings()
            );

            // Configure and execute the request
            Document doc = createConnection(url).get().outputSettings(settings);

            // Extract and set content
            extractAndSetContent(doc, result);

        } catch (IOException e) {
            handleScrapingError(url, e, result);
        } catch (Exception e) {
            handleUnexpectedError(url, e, result);
        }

        return result;
    }

    private org.jsoup.Connection createConnection(String url) {
        org.jsoup.Connection conn = Jsoup.connect(url)
            .userAgent(userAgent)
            .timeout(timeoutMillis)
            .maxBodySize(0) // unlimited
            .followRedirects(true)
            .ignoreHttpErrors(true);

        // Add proxy if configured
        if (proxyHost != null && proxyPort > 0) {
            Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort)
            );
            conn.proxy(proxy);
        }

        return conn;
    }

    private Document.OutputSettings createConnectionSettings() {
        return new Document.OutputSettings()
            .prettyPrint(false)
            .outline(false)
            .indentAmount(0);
    }

    private void extractAndSetContent(Document doc, JobScrapingResult result) {
        String rawHtml = doc.outerHtml();
        String rawText = doc.text();

        result.setRawHtmlContent(rawHtml)
              .setRawTextContent(sanitizeContent(rawText));

        // Add metadata
        result.getMetadata().put("title", doc.title());
        result.getMetadata().put("baseUri", doc.baseUri());
    }

    private void handleScrapingError(String url, IOException e, JobScrapingResult result) {
        logger.error("Failed to scrape URL: {} - {}", url, e.getMessage(), e);
        result.setSuccessful(false)
              .setErrorMessage("Failed to scrape URL: " + e.getMessage());

        // Remove failed connection from pool
        connectionPool.remove(getDomain(url));
    }

    private void handleUnexpectedError(String url, Exception e, JobScrapingResult result) {
        logger.error("Unexpected error while scraping URL: {} - {}", url, e.getMessage(), e);
        result.setSuccessful(false)
              .setErrorMessage("Unexpected error: " + e.getMessage());
    }

    private String getDomain(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }

    @Override
    public boolean supportsUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }
}
