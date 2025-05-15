package io.j13n.core.service.scrape;

import java.util.concurrent.CompletableFuture;
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
        return content.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }
}
