package io.j13n.core.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import io.j13n.core.model.JobSearchResult;

public abstract class AbstractJobSearchService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJobSearchService.class);
    protected static final int DEFAULT_MAX_RESULTS = 10;

    public abstract List<JobSearchResult> searchJobs(String query, String location, boolean isRemoteOnly);

    protected WebSearchRequest createJobSearchRequest(String query, String location, boolean isRemoteOnly) {
        StringBuilder searchQuery = new StringBuilder(query);

        // Add location if provided
        if (location != null && !location.trim().isEmpty()) {
            searchQuery.append(" in ").append(location);
        }

        // Add remote if specified
        if (isRemoteOnly) {
            searchQuery.append(" remote");
        }

        // Add job-specific keywords to improve results
        searchQuery.append(" job application apply");

        return WebSearchRequest.builder()
                .searchTerms(searchQuery.toString())
                .maxResults(DEFAULT_MAX_RESULTS)
                .build();
    }

    protected abstract List<JobSearchResult> parseSearchResults(WebSearchResults searchResults, String source);
}
