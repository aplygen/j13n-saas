package io.j13n.core.service.search.google;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import io.j13n.core.service.search.AbstractSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleSearchService extends AbstractSearchService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSearchService.class);

    private final GoogleCustomWebSearchEngine searchEngine;

    public GoogleSearchService(
            @Value("${google.api.key}") String googleApiKey, @Value("${google.cse.id}") String googleCseId) {
        this.searchEngine = GoogleCustomWebSearchEngine.builder()
                .apiKey(googleApiKey)
                .csi(googleCseId)
                .build();
    }

    @Override
    public WebSearchResults search(String query) {
        logger.info("Performing Google search with query: {}", query);
        try {
            WebSearchRequest request = createSearchRequest(query);
            return searchEngine.search(request);
        } catch (Exception e) {
            logger.error("Error performing Google search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform Google search", e);
        }
    }
}
