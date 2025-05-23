package io.j13n.core.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import org.junit.jupiter.api.Test;

class AbstractSearchServiceTest {

    private static class TestSearchService extends AbstractSearchService {
        @Override
        public WebSearchResults search(String query) {
            return null; // Not needed for this test
        }
    }

    @Test
    void createSearchRequest_ValidQuery_ReturnsRequestWithDefaultMaxResults() {
        // Arrange
        TestSearchService service = new TestSearchService();
        String query = "test query";

        // Act
        WebSearchRequest request = service.createSearchRequest(query);

        // Assert
        assertNotNull(request);
        assertEquals(query, request.searchTerms());
        assertEquals(AbstractSearchService.DEFAULT_MAX_RESULTS, request.maxResults());
    }

    @Test
    void createSearchRequest_EmptyQuery_ReturnsRequestWithEmptyQuery() {
        // Arrange
        TestSearchService service = new TestSearchService();
        String query = "";

        // Act
        WebSearchRequest request = service.createSearchRequest(query);

        // Assert
        assertNotNull(request);
        assertEquals("", request.searchTerms());
        assertEquals(AbstractSearchService.DEFAULT_MAX_RESULTS, request.maxResults());
    }

    @Test
    void createSearchRequest_NullQuery_ReturnsRequestWithNullQuery() {
        // Arrange
        TestSearchService service = new TestSearchService();
        String query = null;

        // Act
        WebSearchRequest request = service.createSearchRequest(query);

        // Assert
        assertNotNull(request);
        assertEquals(null, request.searchTerms());
        assertEquals(AbstractSearchService.DEFAULT_MAX_RESULTS, request.maxResults());
    }
}
