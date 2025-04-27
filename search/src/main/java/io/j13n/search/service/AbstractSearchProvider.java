package io.j13n.search.service;

import io.j13n.commons.dto.ObjectWithUniqueID;
import io.j13n.commons.service.VirtualThreadManager;
import io.j13n.search.model.brave.WebSearchApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchProvider.class);

    @Autowired
    protected VirtualThreadManager virtualThreadManager;

    @Autowired
    protected RestTemplate restTemplate;

    @Override
    public CompletableFuture<ObjectWithUniqueID<WebSearchApiResponse>> searchWithId(
            String query, Map<String, String> params) {
        return search(query, params).thenApply(response -> {
            ObjectWithUniqueID<WebSearchApiResponse> result = new ObjectWithUniqueID<>(response);
            if (result.getHeaders() == null) {
                result.setHeaders(Map.of("query", query));
            } else {
                result.getHeaders().put("query", query);
            }
            return result;
        });
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Encoding", "gzip");
        return headers;
    }

    protected String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(getBaseUrl());
        if (!getBaseUrl().endsWith("/") && !path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);

        if (params != null && !params.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) url.append("&");

                url.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        return url.toString();
    }

    protected void logRequest(String url, HttpHeaders headers) {
        logger.debug("Making request to: {}", url);
        logger.debug("Headers: {}", headers);
    }
}
