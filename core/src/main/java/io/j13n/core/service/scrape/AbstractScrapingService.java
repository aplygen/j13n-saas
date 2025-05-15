package io.j13n.core.service.scrape;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.j13n.core.model.scrape.FormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.j13n.core.model.scrape.JobScrapingResult;

public abstract class AbstractScrapingService {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractScrapingService.class);

    public abstract CompletableFuture<JobScrapingResult> scrapeJobDetails(String url);

    public abstract boolean supportsUrl(String url);

    protected String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        return content.trim().replaceAll("\\s+", " ").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

    public abstract CompletableFuture<List<FormField>> extractFormFields(String url);

    protected boolean isValidFormField(FormField field) {
        return field != null && (field.getName() != null || field.getId() != null) && field.getType() != null;
    }

    protected String sanitizeFormValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("[\u0000-\u001F\u007F-\u009F]", ""); // Remove control characters
    }
}
