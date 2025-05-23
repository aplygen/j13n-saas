package io.j13n.core.service.scrape;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;
import io.j13n.core.model.scrape.FormField;
import io.j13n.core.model.scrape.JobScrapingResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlaywrightScrapingService extends AbstractScrapingService {

    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";
    private static final int VIEWPORT_WIDTH = 1920;
    private static final int VIEWPORT_HEIGHT = 1080;
    private static final int SEMAPHORE_WAIT_SECONDS = 30;

    private final ConcurrentHashMap<String, BrowserContext> contextPool;
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
        this.contextPool = new ConcurrentHashMap<>();
        this.scrapingSemaphore = new Semaphore(maxConcurrentScrapes, true);
    }

    @PostConstruct
    public void initialize() {
        try {
            playwright = Playwright.create();
            browser = createBrowser();
            log.info(
                    "Playwright initialized with browser: {}",
                    browser.browserType().name());
        } catch (Exception e) {
            log.error("Failed to initialize Playwright: {}", e.getMessage(), e);
            throw new PlaywrightException("Failed to initialize Playwright", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            cleanupContextPool();
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
            log.info("Playwright resources cleaned up");
        } catch (Exception e) {
            log.error("Error during Playwright cleanup: {}", e.getMessage(), e);
        }
    }

    private void cleanupContextPool() {
        contextPool.values().forEach(context -> {
            try {
                context.close();
            } catch (Exception e) {
                log.warn("Failed to close browser context: {}", e.getMessage());
            }
        });
        contextPool.clear();
    }

    private Browser createBrowser() {
        BrowserType.LaunchOptions options = createBrowserOptions();
        configureProxyIfNeeded(options);
        return playwright.chromium().launch(options);
    }

    private BrowserType.LaunchOptions createBrowserOptions() {
        return new BrowserType.LaunchOptions().setHeadless(true).setTimeout(timeoutMillis);
    }

    private void configureProxyIfNeeded(BrowserType.LaunchOptions options) {
        if (proxyHost != null && proxyPort > 0) {
            Proxy proxy = new Proxy(proxyHost + ":" + proxyPort);
            if (proxyUsername != null && proxyPassword != null) {
                proxy.setUsername(proxyUsername).setPassword(proxyPassword);
            }
            options.setProxy(proxy);
            log.info("Configured proxy: {}", proxyHost);
        }
    }

    @Override
    @Retryable(
            value = {PlaywrightException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public CompletableFuture<JobScrapingResult> scrapeJobDetails(String url) {
        if (!supportsUrl(url)) {
            return CompletableFuture.completedFuture(createErrorResult(url, "Invalid URL format"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!acquireSemaphore()) {
                    return createErrorResult(url, "Too many concurrent scraping requests");
                }
                return scrapeWithPlaywright(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return createErrorResult(url, "Scraping interrupted: " + e.getMessage());
            } finally {
                scrapingSemaphore.release();
            }
        });
    }

    private boolean acquireSemaphore() throws InterruptedException {
        return scrapingSemaphore.tryAcquire(SEMAPHORE_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    private JobScrapingResult createErrorResult(String url, String errorMessage) {
        return new JobScrapingResult().setSourceUrl(url).setSuccessful(false).setErrorMessage(errorMessage);
    }

    private JobScrapingResult scrapeWithPlaywright(String url) {
        JobScrapingResult result = new JobScrapingResult().setSourceUrl(url).setSuccessful(true);

        String domain = getDomain(url);
        BrowserContext context = getOrCreateContext(domain);

        try {
            scrapePage(url, result, context);
        } catch (PlaywrightException e) {
            handleScrapingError(url, e, result);
            removeContext(domain);
        } catch (Exception e) {
            handleUnexpectedError(url, e, result);
        }

        return result;
    }

    private void scrapePage(String url, JobScrapingResult result, BrowserContext context) {
        try (Page page = context.newPage()) {
            configurePageSettings(page);
            Response response = navigateToPage(page, url);
            validateResponse(response, url);

            page.waitForLoadState(LoadState.NETWORKIDLE);
            extractPageContent(page, result);
        }
    }

    private void configurePageSettings(Page page) {
        page.setDefaultTimeout(timeoutMillis);
        page.setDefaultNavigationTimeout(timeoutMillis);
    }

    private Response navigateToPage(Page page, String url) {
        return page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }

    private void validateResponse(Response response, String url) {
        if (response == null || !response.ok()) {
            throw new PlaywrightException("Failed to load page: " + url);
        }
    }

    private void extractPageContent(Page page, JobScrapingResult result) {
        String rawHtml = page.content();
        String rawText = page.innerText("body");

        result.setRawHtmlContent(rawHtml).setRawTextContent(sanitizeContent(rawText));

        result.getMetadata().put("title", page.title());
        result.getMetadata().put("url", page.url());
        result.getMetadata().put("status", String.valueOf(page.url()));
    }

    private BrowserContext getOrCreateContext(String domain) {
        return contextPool.computeIfAbsent(domain, k -> createNewContext());
    }

    private BrowserContext createNewContext() {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setUserAgent(USER_AGENT)
                .setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
                .setJavaScriptEnabled(true)
                .setBypassCSP(true)
                .setIgnoreHTTPSErrors(true);

        return browser.newContext(options);
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
        result.setSuccessful(false).setErrorMessage("Failed to scrape URL: " + e.getMessage());
    }

    private void handleUnexpectedError(String url, Exception e, JobScrapingResult result) {
        log.error("Unexpected error while scraping URL: {} - {}", url, e.getMessage(), e);
        result.setSuccessful(false).setErrorMessage("Unexpected error: " + e.getMessage());
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

    public String sanitizeContent(String content) {
        if (content == null) return "";
        return content.trim().replaceAll("\\s+", " ").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

    @Override
    @Retryable(
            value = {PlaywrightException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public CompletableFuture<List<FormField>> extractFormFields(String url) {
        if (!supportsUrl(url)) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!acquireSemaphore()) {
                    log.warn("Could not acquire semaphore for form extraction: {}", url);
                    return List.of();
                }
                return extractFormFieldsWithPlaywright(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Form field extraction interrupted: {}", e.getMessage());
                return List.of();
            } finally {
                scrapingSemaphore.release();
            }
        });
    }

    private List<FormField> extractFormFieldsWithPlaywright(String url) {
        List<FormField> formFields = new ArrayList<>();
        String domain = getDomain(url);
        BrowserContext context = getOrCreateContext(domain);

        try {
            extractFormFieldsFromPage(url, formFields, context);
        } catch (PlaywrightException e) {
            log.error("Failed to extract form fields from URL: {} - {}", url, e.getMessage(), e);
            removeContext(domain);
        } catch (Exception e) {
            log.error("Unexpected error while extracting form fields: {} - {}", url, e.getMessage(), e);
        }

        return formFields;
    }

    private void extractFormFieldsFromPage(String url, List<FormField> formFields, BrowserContext context) {
        try (Page page = context.newPage()) {
            configurePageSettings(page);
            Response response = navigateToPage(page, url);
            validateResponse(response, url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            for (ElementHandle form : page.querySelectorAll("form")) {
                extractFieldsFromForm(form, page, formFields);
            }
        }
    }

    private void extractFieldsFromForm(ElementHandle form, Page page, List<FormField> formFields) {
        for (ElementHandle input : form.querySelectorAll("input, select, textarea")) {
            FormField field = createFormField(input, page);
            if (isValidFormField(field)) {
                formFields.add(field);
            }
        }
    }

    private FormField createFormField(ElementHandle input, Page page) {
        FormField field = new FormField();
        setBasicAttributes(field, input);
        setLabel(field, input, page);
        handleSpecialFieldTypes(field, input);
        return field;
    }

    private void setBasicAttributes(FormField field, ElementHandle input) {
        field.setName(sanitizeFormValue(input.getAttribute("name")))
                .setId(sanitizeFormValue(input.getAttribute("id")))
                .setType(input.evaluate("el => el.type || el.tagName.toLowerCase()")
                        .toString())
                .setRequired(Boolean.parseBoolean(input.getAttribute("required")))
                .setPlaceholder(sanitizeFormValue(input.getAttribute("placeholder")))
                .setDefaultValue(sanitizeFormValue(input.getAttribute("value")))
                .setSelector(generateSelector(input));
    }

    private void setLabel(FormField field, ElementHandle input, Page page) {
        String id = field.getId();
        if (id != null && !id.isEmpty()) {
            ElementHandle label = page.querySelector("label[for='" + id + "']");
            if (label != null) {
                field.setLabel(sanitizeFormValue(label.innerText()));
            } else {
                findAndSetNearestLabel(field, input);
            }
        }
    }

    private void findAndSetNearestLabel(FormField field, ElementHandle input) {
        ElementHandle nearestLabel = input.evaluateHandle("el => { " + "let label = el.closest('label') || "
                        + "el.previousElementSibling?.tagName === 'LABEL' ? el.previousElementSibling : null; "
                        + "return label; }")
                .asElement();
        if (nearestLabel != null) {
            field.setLabel(sanitizeFormValue(nearestLabel.innerText()));
        }
    }

    private void handleSpecialFieldTypes(FormField field, ElementHandle input) {
        if (field.getType().equalsIgnoreCase("select")) {
            handleSelectField(field, input);
        } else if (field.getType().equals("radio") || field.getType().equals("checkbox")) {
            handleOptionField(field, input);
        } else if (field.getType().equalsIgnoreCase("file")) {
            handleFileField(field, input);
        }
    }

    private void handleSelectField(FormField field, ElementHandle input) {
        List<String> options = new ArrayList<>();
        for (ElementHandle option : input.querySelectorAll("option")) {
            String optionText = sanitizeFormValue(option.innerText());
            if (!optionText.isEmpty()) {
                options.add(optionText);
            }
        }
        field.setOptions(options.toArray(new String[0]));
    }

    private void handleOptionField(FormField field, ElementHandle input) {
        String name = field.getName();
        if (name != null && !name.isEmpty()) {
            List<String> options = new ArrayList<>();
            ElementHandle form = input.querySelector("ancestor::form");
            if (form != null) {
                for (ElementHandle related : form.querySelectorAll("input[name='" + name + "']")) {
                    ElementHandle label = related.querySelector("label[for='" + related.getAttribute("id") + "']");
                    if (label != null) {
                        options.add(sanitizeFormValue(label.innerText()));
                    }
                }
                field.setOptions(options.toArray(new String[0]));
            }
        }
    }

    private void handleFileField(FormField field, ElementHandle input) {
        String acceptAttr = input.getAttribute("accept");
        if (acceptAttr != null && !acceptAttr.isEmpty()) {
            field.getMetadata().put("accept", acceptAttr);
        }
        field.getMetadata().put("isFileUpload", "true");

        String fieldId = field.getId().toLowerCase();
        String fieldName = field.getName().toLowerCase();
        String fieldLabel = field.getLabel() != null ? field.getLabel().toLowerCase() : "";

        if (fieldId.contains("resume") || fieldName.contains("resume") || fieldLabel.contains("resume")) {
            field.getMetadata().put("fileType", "resume");
        } else if (fieldId.contains("cover") || fieldName.contains("cover") || fieldLabel.contains("cover letter")) {
            field.getMetadata().put("fileType", "coverLetter");
        }
    }

    public String sanitizeFormValue(String value) {
        if (value == null) return "";
        return value.trim();
    }

    public boolean isValidFormField(FormField field) {
        return field != null && (field.getName() != null || field.getId() != null) && field.getType() != null;
    }

    private String generateSelector(ElementHandle element) {
        try {
            return element.evaluate("el => { " + "let path = []; "
                            + "while (el.nodeType === 1) { "
                            + "  let selector = el.nodeName.toLowerCase(); "
                            + "  if (el.id) { "
                            + "    selector += '#' + el.id; "
                            + "    path.unshift(selector); "
                            + "    break; "
                            + "  } else { "
                            + "    let sibling = el, nth = 1; "
                            + "    while (sibling.previousElementSibling) { "
                            + "      sibling = sibling.previousElementSibling; "
                            + "      if (sibling.nodeName.toLowerCase() === selector) nth++; "
                            + "    } "
                            + "    if (nth !== 1) selector += ':nth-of-type(' + nth + ')'; "
                            + "  } "
                            + "  path.unshift(selector); "
                            + "  el = el.parentNode; "
                            + "} "
                            + "return path.join(' > '); "
                            + "}")
                    .toString();
        } catch (Exception e) {
            log.warn("Failed to generate selector for element", e);
            return null;
        }
    }
}
