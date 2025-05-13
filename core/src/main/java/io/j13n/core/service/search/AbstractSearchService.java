package io.j13n.core.service.search;

import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;

public abstract class AbstractSearchService {

    protected static final int DEFAULT_MAX_RESULTS = 10;

    public abstract WebSearchResults search(String query);

    protected WebSearchRequest createSearchRequest(String query) {
        return WebSearchRequest.builder()
                .searchTerms(query)
                .maxResults(DEFAULT_MAX_RESULTS)
                .build();
    }
}
