package io.j13n.search.service;

import io.j13n.search.model.brave.WebSearchApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class BraveSearchProvider extends AbstractSearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(BraveSearchProvider.class);
    private static final String PROVIDER_NAME = "brave";
    private static final String API_PATH = "res/v1/web/search";

    @Value("${brave.search.api.url:https://api.search.brave.com}")
    private String baseUrl;

    @Value("${brave.search.api.key}")
    private String apiKey;

    @Override
    public CompletableFuture<WebSearchApiResponse> search(String query, Map<String, String> params) {
        return virtualThreadManager.submitTask((Supplier<WebSearchApiResponse>) () -> {
            try {
                Map<String, String> queryParams = new HashMap<>(params != null ? params : new HashMap<>());

                queryParams.put("q", query);

                String url = buildUrl(API_PATH, queryParams);

                HttpHeaders headers = createHeaders();
                headers.set("X-Subscription-Token", apiKey);

                logRequest(url, headers);

                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<WebSearchApiResponse> response =
                        restTemplate.exchange(url, HttpMethod.GET, requestEntity, WebSearchApiResponse.class);

                return response.getBody();
            } catch (RestClientException e) {
                logger.error("Error searching Brave API: {}", e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }
}
