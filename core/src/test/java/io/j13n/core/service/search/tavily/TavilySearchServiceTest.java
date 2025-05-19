package io.j13n.core.service.search.tavily;

import dev.langchain4j.web.search.WebSearchInformationResult;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TavilySearchServiceTest {

    @Mock
    private TavilyWebSearchEngine mockSearchEngine;

    private TavilySearchService searchService;

    @BeforeEach
    void setUp() {
        // Initialize with a dummy API key
        searchService = new TavilySearchService("dummy-api-key");
        // Replace the real search engine with our mock
        ReflectionTestUtils.setField(searchService, "searchEngine", mockSearchEngine);
    }

    @Test
    void search_SuccessfulSearch_ReturnsResults() {
        // Arrange
        String query = "test query";
        WebSearchOrganicResult mockResult = new WebSearchOrganicResult(
                "Test Result",
                URI.create("https://example.com"),
                "This is a test result",
                "text/html"
        );

        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(500L, 1, metadata);
        WebSearchResults expectedResults = new WebSearchResults(info, List.of(mockResult));

        when(mockSearchEngine.search(any(WebSearchRequest.class))).thenReturn(expectedResults);

        // Act
        WebSearchResults actualResults = searchService.search(query);

        // Assert
        assertNotNull(actualResults);
        assertEquals(1, actualResults.results().size());
        assertEquals("Test Result", actualResults.results().get(0).title());
        assertEquals("https://example.com", actualResults.results().get(0).url().toString());
        assertEquals("This is a test result", actualResults.results().get(0).snippet());

        verify(mockSearchEngine).search(any(WebSearchRequest.class));
    }

    @Test
    void search_ApiError_ThrowsRuntimeException() {
        // Arrange
        String query = "test query";
        when(mockSearchEngine.search(any(WebSearchRequest.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> searchService.search(query));

        assertEquals("Failed to perform Tavily search", exception.getMessage());
        verify(mockSearchEngine).search(any(WebSearchRequest.class));
    }

    @Test
    void search_NullQuery_HandledGracefully() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
        WebSearchResults emptyResults = new WebSearchResults(info, Collections.emptyList());
        when(mockSearchEngine.search(any(WebSearchRequest.class))).thenReturn(emptyResults);

        // Act
        WebSearchResults results = searchService.search(null);

        // Assert
        assertNotNull(results);
        assertTrue(results.results().isEmpty());
        verify(mockSearchEngine).search(any(WebSearchRequest.class));
    }

    @Test
    void search_EmptyQuery_ReturnsEmptyResults() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        WebSearchInformationResult info = new WebSearchInformationResult(100L, 0, metadata);
        WebSearchResults emptyResults = new WebSearchResults(info, Collections.emptyList());
        when(mockSearchEngine.search(any(WebSearchRequest.class))).thenReturn(emptyResults);

        // Act
        WebSearchResults results = searchService.search("");

        // Assert
        assertNotNull(results);
        assertTrue(results.results().isEmpty());
        verify(mockSearchEngine).search(any(WebSearchRequest.class));
    }
}
