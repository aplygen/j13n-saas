package io.j13n.core.service.search.google;

import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import io.j13n.core.model.JobSearchResult;
import io.j13n.core.service.search.AbstractJobSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleJobSearchService extends AbstractJobSearchService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleJobSearchService.class);
    private static final String SOURCE = "Google";

    private static final Pattern COMPANY_PATTERN = Pattern.compile("at\\s+([\\w\\s&]+)|\\|\\s*([\\w\\s&]+)");

    private final GoogleCustomWebSearchEngine searchEngine;

    public GoogleJobSearchService(
            @Value("${google.api.key}") String googleApiKey,
            @Value("${google.cse.id}") String googleCseId) {
        Objects.requireNonNull(googleApiKey, "Google API key must not be null");
        Objects.requireNonNull(googleCseId, "Google CSE ID must not be null");

        this.searchEngine = GoogleCustomWebSearchEngine.builder()
                .apiKey(googleApiKey)
                .csi(googleCseId)
                .build();
    }

    @Override
    public List<JobSearchResult> searchJobs(String query, String location, boolean isRemoteOnly) {
        logger.info("Performing job search with query: {}, location: {}, remoteOnly: {}",
                   query, location, isRemoteOnly);

        try {
            WebSearchRequest request = createJobSearchRequest(query, location, isRemoteOnly);
            WebSearchResults results = searchEngine.search(request);
            return parseSearchResults(results, SOURCE);
        } catch (Exception e) {
            logger.error("Error performing job search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform job search", e);
        }
    }

    @Override
    protected List<JobSearchResult> parseSearchResults(WebSearchResults searchResults, String source) {
        List<JobSearchResult> jobResults = new ArrayList<>();

        for (WebSearchOrganicResult result : searchResults.results()) {
            String title = result.getTitle();
            String description = result.getSnippet();
            String url = result.getUrl();

            // Extract company name from title using pattern
            String company = extractCompany(title);

            // Create job result
            JobSearchResult job = new JobSearchResult();
            job.setTitle(cleanTitle(title));
            job.setCompany(company);
            job.setDescription(description);
            job.setApplicationUrl(url);
            job.setSource(source);
            job.setPostedDate(LocalDateTime.now());
            job.setRemote(title.toLowerCase().contains("remote") ||
                         description.toLowerCase().contains("remote"));

            jobResults.add(job);
        }

        return jobResults;
    }

    private String extractCompany(String title) {
        Matcher matcher = COMPANY_PATTERN.matcher(title);
        if (matcher.find())
            return matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
        return "Unknown Company";
    }

    private String cleanTitle(String title) {
        return title.replaceAll("\\|.*$", "")  // Remove everything after |
                   .replaceAll("at\\s+[\\w\\s&]+$", "") // Remove "at Company"
                   .trim();
    }
}
