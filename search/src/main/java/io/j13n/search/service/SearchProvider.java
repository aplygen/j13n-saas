package io.j13n.search.service;

import io.j13n.commons.dto.ObjectWithUniqueID;
import io.j13n.search.model.brave.WebSearchApiResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface SearchProvider {

    CompletableFuture<WebSearchApiResponse> search(String query, Map<String, String> params);

    CompletableFuture<ObjectWithUniqueID<WebSearchApiResponse>> searchWithId(String query, Map<String, String> params);

    String getProviderName();

    String getBaseUrl();
}
