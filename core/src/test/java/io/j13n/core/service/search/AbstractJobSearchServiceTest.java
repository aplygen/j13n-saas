package io.j13n.core.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import dev.langchain4j.web.search.WebSearchInformationResult;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import io.j13n.core.model.JobSearchResult;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractJobSearchServiceTest {

    private static class TestSearchService extends AbstractSearchService {
        private WebSearchResults mockResults;

        @Override
        public WebSearchResults search(String query) {
            if (mockResults != null) {
                return mockResults;
            }
            Map<String, Object> metadata = new HashMap<>();
            WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
            return new WebSearchResults(info, Collections.emptyList());
        }

        public void setMockResults(WebSearchResults results) {
            this.mockResults = results;
        }
    }

    private static class TestJobSearchService extends AbstractJobSearchService {
        private final TestSearchService searchService;

        public TestJobSearchService() {
            this.searchService = new TestSearchService();
        }

        @Override
        protected List<JobSearchResult> parseSearchResults(WebSearchResults searchResults, String source) {
            // Simple implementation that creates a job result for each search result
            return searchResults.results().stream()
                    .map(result -> {
                        JobSearchResult job = new JobSearchResult();
                        job.setTitle(result.title());
                        job.setDescription(result.snippet());
                        job.setApplicationUrl(result.url());
                        job.setSource(source);
                        job.setPostedDate(LocalDateTime.now());
                        return job;
                    })
                    .toList();
        }

        @Override
        public List<JobSearchResult> searchJobs(String query, String location, boolean isRemoteOnly) {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be null or empty");
            }
            WebSearchRequest request = createJobSearchRequest(query, location, isRemoteOnly);
            WebSearchResults results = searchService.search(request.searchTerms());
            return parseSearchResults(results, "Test");
        }

        public void setMockResults(WebSearchResults results) {
            searchService.setMockResults(results);
        }
    }

    private TestJobSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = spy(new TestJobSearchService());
    }

    @Test
    void searchJobs_ValidRequest_ReturnsJobResults() {
        // Arrange
        String query = "software engineer";
        String location = "San Francisco";
        boolean isRemoteOnly = true;

        WebSearchOrganicResult mockResult = new WebSearchOrganicResult(
                "Software Engineer Position",
                URI.create("https://example.com/job"),
                "Great opportunity for a software engineer",
                "text/html");

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(500L, 1, metadata);
        WebSearchResults searchResults = new WebSearchResults(info, List.of(mockResult));
        searchService.setMockResults(searchResults);

        // Act
        List<JobSearchResult> results = searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        JobSearchResult job = results.get(0);
        assertEquals("Software Engineer Position", job.getTitle());
        assertEquals("Great opportunity for a software engineer", job.getDescription());
        assertEquals("https://example.com/job", job.getApplicationUrl().toString());
    }

    @Test
    void searchJobs_EmptyResults_ReturnsEmptyList() {
        // Arrange
        String query = "nonexistent job";
        String location = "";
        boolean isRemoteOnly = false;

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
        WebSearchResults emptyResults = new WebSearchResults(info, Collections.emptyList());
        searchService.setMockResults(emptyResults);

        // Act
        List<JobSearchResult> results = searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchJobs_NullQuery_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> searchService.searchJobs(null, "location", false));
    }

    @Test
    void searchJobs_EmptyQuery_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> searchService.searchJobs("", "location", false));
    }

    @Test
    void searchJobs_ValidRequest_CallsParseSearchResults() {
        // Arrange
        String query = "software engineer";
        String location = "New York";
        boolean isRemoteOnly = false;

        WebSearchOrganicResult mockResult = new WebSearchOrganicResult(
                "Software Engineer", URI.create("https://example.com/job"), "Job description", "text/html");

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(500L, 1, metadata);
        WebSearchResults searchResults = new WebSearchResults(info, List.of(mockResult));
        searchService.setMockResults(searchResults);

        // Act
        searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        verify(searchService).parseSearchResults(any(WebSearchResults.class), any(String.class));
    }
}
