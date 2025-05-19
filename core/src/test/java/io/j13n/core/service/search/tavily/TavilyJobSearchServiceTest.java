package io.j13n.core.service.search.tavily;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.langchain4j.web.search.WebSearchInformationResult;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

@ExtendWith(MockitoExtension.class)
class TavilyJobSearchServiceTest {

    @Mock
    private TavilyWebSearchEngine mockSearchEngine;

    private TavilyJobSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new TavilyJobSearchService("dummy-api-key");
        ReflectionTestUtils.setField(searchService, "searchEngine", mockSearchEngine);
    }

    @Test
    void searchJobs_ValidQuery_ReturnsJobResults() {
        // Arrange
        String query = "software engineer";
        String location = "San Francisco";
        boolean isRemoteOnly = true;

        WebSearchOrganicResult mockResult = new WebSearchOrganicResult(
                "Senior Software Engineer at Google | Remote",
                URI.create("https://careers.google.com/jobs/123"),
                "Exciting opportunity for a Senior Software Engineer. Remote work available. " +
                "Join our team to work on cutting-edge technology.",
                "text/html"
        );

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(500L, 1, metadata);
        WebSearchResults searchResults = new WebSearchResults(info, List.of(mockResult));

        when(mockSearchEngine.search(anyString())).thenReturn(searchResults);

        // Act
        var jobResults = searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        assertNotNull(jobResults);
        assertEquals(1, jobResults.size());
        var job = jobResults.get(0);
        assertEquals("Senior Software Engineer", job.getTitle());
        assertEquals("Google", job.getCompany());
        assertTrue(job.isRemote());
        assertEquals("https://careers.google.com/jobs/123", job.getApplicationUrl().toString());
    }

    @Test
    void searchJobs_NoResults_ReturnsEmptyList() {
        // Arrange
        String query = "nonexistent job";
        String location = "nowhere";
        boolean isRemoteOnly = false;

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
        WebSearchResults emptyResults = new WebSearchResults(info, Collections.emptyList());

        when(mockSearchEngine.search(anyString())).thenReturn(emptyResults);

        // Act
        var results = searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchJobs_InvalidJobPosting_FiltersOutInvalidResults() {
        // Arrange
        String query = "software engineer";
        String location = "New York";
        boolean isRemoteOnly = false;

        WebSearchOrganicResult invalidResult = new WebSearchOrganicResult(
                "Search Jobs - Career Opportunities",
                URI.create("https://example.com/search-jobs"),
                "Find your next career opportunity. Browse thousands of jobs.",
                "text/html"
        );

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(500L, 1, metadata);
        WebSearchResults searchResults = new WebSearchResults(info, List.of(invalidResult));

        when(mockSearchEngine.search(anyString())).thenReturn(searchResults);

        // Act
        var results = searchService.searchJobs(query, location, isRemoteOnly);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchJobs_ApiError_ThrowsRuntimeException() {
        // Arrange
        String query = "software engineer";
        String location = "London";
        boolean isRemoteOnly = false;

        when(mockSearchEngine.search(anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> searchService.searchJobs(query, location, isRemoteOnly));

        assertEquals("Failed to perform Tavily job search", exception.getMessage());
    }

    @Test
    void searchJobs_LongQueryAndLocation_TruncatesAppropriately() {
        // Arrange
        String longQuery = "a".repeat(500);
        String longLocation = "b".repeat(500);
        boolean isRemoteOnly = true;

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
        WebSearchResults emptyResults = new WebSearchResults(info, Collections.emptyList());

        when(mockSearchEngine.search(anyString())).thenReturn(emptyResults);

        // Act
        var results = searchService.searchJobs(longQuery, longLocation, isRemoteOnly);

        // Assert
        assertNotNull(results);
        verify(mockSearchEngine).search(anyString());
    }
}