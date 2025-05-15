package io.j13n.core.model;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@FieldNameConstants
public class JobSearchResult {
    private String title;
    private String company;
    private String location;
    private String description;
    private URI applicationUrl;
    private String source; // e.g., "LinkedIn", "Indeed", etc.
    private LocalDateTime postedDate;
    private boolean isRemote;

    public JobSearchResult() {}

    public JobSearchResult(
            String title,
            String company,
            String location,
            String description,
            URI applicationUrl,
            String source,
            LocalDateTime postedDate,
            boolean isRemote) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.postedDate = postedDate;
        this.isRemote = isRemote;
    }
}
