package io.j13n.core.service.search.tavily;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import io.j13n.core.service.search.AbstractSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TavilySearchService extends AbstractSearchService {

    private static final Logger logger = LoggerFactory.getLogger(TavilySearchService.class);

    private final TavilyWebSearchEngine searchEngine;

    public TavilySearchService(@Value("${tavily.api.key}") String tavilyApiKey) {
        this.searchEngine = TavilyWebSearchEngine.builder()
                .apiKey(tavilyApiKey)
                .searchDepth("advanced")
                .build();
    }

    @Override
    public WebSearchResults search(String query) {
        logger.info("Performing Tavily search with query: {}", query);
        try {
            WebSearchRequest request = createSearchRequest(query);
            return searchEngine.search(request);
        } catch (Exception e) {
            logger.error("Error performing Tavily search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform Tavily search", e);
        }
    }
}
