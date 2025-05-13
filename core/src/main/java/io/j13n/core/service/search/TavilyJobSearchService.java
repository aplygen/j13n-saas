package io.j13n.core.service.search;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import io.j13n.core.model.JobSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class TavilyJobSearchService extends TavilySearchService {

    private static final Logger logger = LoggerFactory.getLogger(TavilyJobSearchService.class);
    private static final Pattern COMPANY_PATTERN = Pattern.compile("at\\s+([\\w\\s&]+)|\\|\\s*([\\w\\s&]+)");
    private static final String SOURCE = "Tavily";
    private static final int MAX_RESULTS = 10;
    private static final int MAX_QUERY_LENGTH = 400;

    private static final Set<String> JOB_DOMAINS = new HashSet<>(Arrays.asList(
            "linkedin.com/jobs",
            "indeed.com",
            "glassdoor.com",
            "careers.",
            "jobs.",
            "workday.com",
            "lever.co",
            "greenhouse.io"
    ));

    private static final Set<String> EXCLUDED_DOMAINS = new HashSet<>(Arrays.asList(
            "facebook.com",
            "instagram.com",
            "twitter.com",
            "youtube.com"
    ));

    public TavilyJobSearchService(@Value("${tavily.api.key}") String tavilyApiKey) {
        super(tavilyApiKey);
    }

    public List<JobSearchResult> searchJobs(String query, String location, boolean isRemoteOnly) {
        logger.info("Performing Tavily job search with query: {}, location: {}, remoteOnly: {}",
                   query, location, isRemoteOnly);

        String enhancedQuery = buildOptimizedQuery(query, location, isRemoteOnly);
        logger.debug("Enhanced query (length: {}): {}", enhancedQuery.length(), enhancedQuery);

        try {
            WebSearchRequest request = WebSearchRequest.builder()
                    .searchTerms(enhancedQuery)
                    .maxResults(MAX_RESULTS)
                    .build();

            WebSearchResults results = search(request.searchTerms());
            return parseJobResults(results);
        } catch (Exception e) {
            logger.error("Error performing Tavily job search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform Tavily job search", e);
        }
    }

    private String buildOptimizedQuery(String query, String location, boolean isRemoteOnly) {
        // Start with core search terms (truncate if too long)
        String baseQuery = truncateIfNeeded(query.trim(), 100);
        StringBuilder queryBuilder = new StringBuilder(baseQuery);
        int remainingLength = MAX_QUERY_LENGTH - queryBuilder.length();

        // Add location if there's space (with quotes for exact matching)
        if (location != null && !location.trim().isEmpty() && remainingLength > 20) {
            String locationTerm = String.format(" in \"%s\"", truncateIfNeeded(location.trim(), 30));
            if (queryBuilder.length() + locationTerm.length() <= MAX_QUERY_LENGTH) {
                queryBuilder.append(locationTerm);
            }
        }

        // Add remote work terms if requested
        if (isRemoteOnly && queryBuilder.length() + 25 <= MAX_QUERY_LENGTH) {
            queryBuilder.append(" \"remote work\"");
        }

        // Calculate remaining space for job-specific terms
        remainingLength = MAX_QUERY_LENGTH - queryBuilder.length() - 30; // Reserve 30 chars for safety

        // Add job-specific terms if there's space
        if (remainingLength > 0) {
            String jobTerms = " (job OR position)";
            if (remainingLength >= jobTerms.length()) {
                queryBuilder.append(jobTerms);

                // Add application terms if there's still space
                String applyTerms = " (apply OR application)";
                if (queryBuilder.length() + applyTerms.length() <= MAX_QUERY_LENGTH) {
                    queryBuilder.append(applyTerms);
                }
            }
        }

        String finalQuery = queryBuilder.toString().trim();

        // Double-check length and truncate if somehow still too long
        if (finalQuery.length() > MAX_QUERY_LENGTH) {
            finalQuery = finalQuery.substring(0, MAX_QUERY_LENGTH - 3) + "...";
        }

        return finalQuery;
    }

    private String truncateIfNeeded(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private List<JobSearchResult> parseJobResults(WebSearchResults results) {
        List<JobSearchResult> jobResults = new ArrayList<>();

        results.results().forEach(result -> {
            try {
                // Extract and validate the job information
                String title = result.getTitle();
                String description = result.getSnippet();
                String url = result.getUrl();

                if (isValidJobPosting(title, description, url)) {
                    JobSearchResult job = createJobResult(title, description, url);
                    jobResults.add(job);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse job result: {}", e.getMessage());
            }
        });

        return jobResults;
    }

    private boolean isValidJobPosting(String title, String description, String url) {
        if (title == null || description == null || url == null) {
            return false;
        }

        // Check if it's a direct job link
        if (!isDirectJobLink(url)) {
            return false;
        }

        // Verify it's not a job search page or list
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("search jobs") || lowerTitle.contains("job list") ||
            lowerTitle.contains("career opportunities")) {
            return false;
        }

        return true;
    }

    private JobSearchResult createJobResult(String title, String description, String url) {
        JobSearchResult job = new JobSearchResult();
        job.setTitle(cleanTitle(title));
        job.setCompany(extractCompany(title));
        job.setDescription(description);
        job.setApplicationUrl(url);
        job.setSource(SOURCE);
        job.setPostedDate(LocalDateTime.now());
        job.setRemote(isRemoteJob(title, description));
        return job;
    }

    private String extractCompany(String title) {
        Matcher matcher = COMPANY_PATTERN.matcher(title);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
        }
        return "Unknown Company";
    }

    private String cleanTitle(String title) {
        return title.replaceAll("\\|.*$", "")  // Remove everything after |
                   .replaceAll("at\\s+[\\w\\s&]+$", "") // Remove "at Company"
                   .replaceAll("\\([^)]*\\)", "") // Remove parentheses and their contents
                   .trim();
    }

    private boolean isRemoteJob(String title, String description) {
        String combined = (title + " " + description).toLowerCase();
        return combined.contains("remote") ||
               combined.contains("work from home") ||
               combined.contains("wfh") ||
               combined.contains("virtual position") ||
               combined.contains("remote-first") ||
               combined.contains("fully remote");
    }

    private boolean isDirectJobLink(final String url) {
        final String lowerUrl = url.toLowerCase();
        return (lowerUrl.contains("/jobs/") ||
                lowerUrl.contains("/careers/") ||
                lowerUrl.contains("/job/") ||
                lowerUrl.contains("apply") ||
                lowerUrl.contains("position")) &&
               JOB_DOMAINS.stream().anyMatch(domain -> lowerUrl.contains(domain.toLowerCase()));
    }
}