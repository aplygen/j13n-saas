package io.j13n.core.service.scrape;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;
import io.j13n.core.model.scrape.JobScrapingResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PlaywrightScrapingService extends AbstractScrapingService {

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private final ConcurrentHashMap<String, BrowserContext> contextPool = new ConcurrentHashMap<>();
    private final Semaphore scrapingSemaphore;
    private Playwright playwright;
    private Browser browser;
    @Value("${scraping.max-concurrent:5}")
    private int maxConcurrentScrapes;

    @Value("${scraping.timeout:30000}")
    private int timeoutMillis;

    @Value("${scraping.proxy.host:#{null}}")
    private String proxyHost;

    @Value("${scraping.proxy.port:0}")
    private int proxyPort;

    @Value("${scraping.proxy.username:#{null}}")
    private String proxyUsername;

    @Value("${scraping.proxy.password:#{null}}")
    private String proxyPassword;

    public PlaywrightScrapingService() {
        this.scrapingSemaphore = new Semaphore(maxConcurrentScrapes, true);
    }

    @PostConstruct
    public void initialize() {
        playwright = Playwright.create();
        browser = createBrowser();
        log.info("Playwright initialized with browser: {}", browser.browserType().name());
    }

    @PreDestroy
    public void cleanup() {
        contextPool.values().forEach(BrowserContext::close);
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("Playwright resources cleaned up");
    }

    private Browser createBrowser() {

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setTimeout(timeoutMillis);

        if (proxyHost != null && proxyPort > 0) {
            Proxy proxy = new Proxy(proxyHost + ":" + proxyPort);
            if (proxyUsername != null && proxyPassword != null) {
                proxy.setUsername(proxyUsername);
                proxy.setPassword(proxyPassword);
            }
            options.setProxy(proxy);
            log.info("Configured proxy: {}", proxyHost);
        }

        return playwright.chromium().launch(options);
    }

    @Override
    @Retryable(
        value = { PlaywrightException.class },
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
                if (!scrapingSemaphore.tryAcquire(30, TimeUnit.SECONDS)) {
                    return new JobScrapingResult()
                        .setSourceUrl(url)
                        .setSuccessful(false)
                        .setErrorMessage("Too many concurrent scraping requests");
                }

                return scrapeWithPlaywright(url);
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

    private JobScrapingResult scrapeWithPlaywright(String url) {
        JobScrapingResult result = new JobScrapingResult()
            .setSourceUrl(url)
            .setSuccessful(true);

        String domain = getDomain(url);
        BrowserContext context = getOrCreateContext(domain);

        try {
            try (Page page = context.newPage()) {
                page.setDefaultTimeout(timeoutMillis);
                page.setDefaultNavigationTimeout(timeoutMillis);

                Response response = page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE));

                if (response == null || !response.ok()) {
                    throw new PlaywrightException("Failed to load page: " + url);
                }

                page.waitForLoadState(LoadState.NETWORKIDLE);

                String rawHtml = page.content();
                String rawText = page.innerText("body");

                result.setRawHtmlContent(rawHtml)
                        .setRawTextContent(sanitizeContent(rawText));

                result.getMetadata().put("title", page.title());
                result.getMetadata().put("url", page.url());
                result.getMetadata().put("status", String.valueOf(response.status()));

            }

        } catch (PlaywrightException e) {
            handleScrapingError(url, e, result);
            removeContext(domain);
        } catch (Exception e) {
            handleUnexpectedError(url, e, result);
        }

        return result;
    }

    private BrowserContext getOrCreateContext(String domain) {
        return contextPool.computeIfAbsent(domain, k -> {
            Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
                .setJavaScriptEnabled(true)
                .setBypassCSP(true)
                .setIgnoreHTTPSErrors(true);

            return browser.newContext(options);
        });
    }

    private void removeContext(String domain) {
        BrowserContext context = contextPool.remove(domain);
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                log.warn("Failed to close browser context for domain: {}", domain, e);
            }
        }
    }

    private void handleScrapingError(String url, PlaywrightException e, JobScrapingResult result) {
        log.error("Failed to scrape URL: {} - {}", url, e.getMessage(), e);
        result.setSuccessful(false)
              .setErrorMessage("Failed to scrape URL: " + e.getMessage());
    }

    private void handleUnexpectedError(String url, Exception e, JobScrapingResult result) {
        log.error("Unexpected error while scraping URL: {} - {}", url, e.getMessage(), e);
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
