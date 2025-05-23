package io.j13n.core.model.scrape;

import io.j13n.core.model.JobSearchResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@FieldNameConstants
public class JobScrapingResult {
    private String sourceUrl;
    private String rawHtmlContent;
    private String rawTextContent;
    private LocalDateTime scrapedAt;
    private Map<String, String> metadata;
    private boolean successful;
    private String errorMessage;
    private JobSearchResult jobSearchResult;

    public JobScrapingResult() {
        this.scrapedAt = LocalDateTime.now();
        this.metadata = new HashMap<>();
        this.successful = true;
    }
}
