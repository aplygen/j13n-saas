package io.j13n.core.model;

import java.time.LocalDateTime;

/**
 * Model class representing a job search result
 */
public class JobSearchResult {
    private String title;
    private String company;
    private String location;
    private String description;
    private String applicationUrl;
    private String source; // e.g., "LinkedIn", "Indeed", etc.
    private LocalDateTime postedDate;
    private boolean isRemote;

    // Default constructor
    public JobSearchResult() {}

    // All-args constructor
    public JobSearchResult(String title, String company, String location, String description,
                         String applicationUrl, String source, LocalDateTime postedDate, boolean isRemote) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.postedDate = postedDate;
        this.isRemote = isRemote;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDateTime postedDate) {
        this.postedDate = postedDate;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    @Override
    public String toString() {
        return "JobSearchResult{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", applicationUrl='" + applicationUrl + '\'' +
                ", source='" + source + '\'' +
                ", postedDate=" + postedDate +
                ", isRemote=" + isRemote +
                '}';
    }
}