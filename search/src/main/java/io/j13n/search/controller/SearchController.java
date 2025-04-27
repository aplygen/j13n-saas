package io.j13n.search.controller;

import io.j13n.commons.dto.ObjectWithUniqueID;
import io.j13n.search.dto.SearchResultDTO;
import io.j13n.search.model.brave.WebSearchApiResponse;
import io.j13n.search.service.BraveSearchProvider;
import io.j13n.search.utils.ResponseEntityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private BraveSearchProvider braveSearchProvider;

    @Value("${brave.search.default.country:US}")
    private String defaultCountry;

    @Value("${brave.search.default.search_lang:en}")
    private String defaultSearchLang;

    @Value("${brave.search.default.count:20}")
    private String defaultCount;

    @Value("${brave.search.default.safesearch:moderate}")
    private String defaultSafesearch;

    @GetMapping
    public CompletableFuture<ResponseEntity<WebSearchApiResponse>> search(
            @RequestParam String query,
            Map<String, String> allParams,
            @RequestHeader(value = "If-None-Match", required = false) String eTag,
            HttpServletRequest request) {
        logger.info("Search request received for query: {}", query);

        Map<String, String> params = new HashMap<>();
        params.put("country", allParams.getOrDefault("country", defaultCountry));
        params.put("search_lang", allParams.getOrDefault("search_lang", defaultSearchLang));
        params.put("count", allParams.getOrDefault("count", defaultCount));
        params.put("offset", allParams.getOrDefault("offset", "0"));
        params.put("safesearch", allParams.getOrDefault("safesearch", defaultSafesearch));
        if (allParams.containsKey("freshness")) {
            params.put("freshness", allParams.get("freshness"));
        }

        return braveSearchProvider
                .searchWithId(query, params)
                .thenApply(response -> {
                    SearchResultDTO dto = new SearchResultDTO(response.getObject(), query, braveSearchProvider.getProviderName())
                            .setIpAddress(request.getRemoteAddr())
                            .setUserAgent(request.getHeader("User-Agent"));

                    logger.info(
                            "Search completed for query: {}, provider: {}, results: {}, ID: {}",
                            query,
                            braveSearchProvider.getProviderName(),
                            response.getObject().getWeb() != null && response.getObject().getWeb().getResults() != null
                                    ? response.getObject().getWeb().getResults().size()
                                    : 0,
                            response.getUniqueId());

                    return ResponseEntityUtils.makeResponseEntity(response, eTag, 300);
                })
                .exceptionally(ex -> {
                    logger.error("Error performing search: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/with-id")
    public CompletableFuture<ResponseEntity<ObjectWithUniqueID<WebSearchApiResponse>>> searchWithId(
            @RequestParam String query,
            Map<String, String> allParams,
            @RequestHeader(value = "If-None-Match", required = false) String eTag,
            HttpServletRequest request) {
        logger.info("Search with ID request received for query: {}", query);

        Map<String, String> params = new HashMap<>();
        params.put("country", allParams.getOrDefault("country", defaultCountry));
        params.put("search_lang", allParams.getOrDefault("search_lang", defaultSearchLang));
        params.put("count", allParams.getOrDefault("count", defaultCount));
        params.put("offset", allParams.getOrDefault("offset", "0"));
        params.put("safesearch", allParams.getOrDefault("safesearch", defaultSafesearch));
        if (allParams.containsKey("freshness")) {
            params.put("freshness", allParams.get("freshness"));
        }

        return braveSearchProvider
                .searchWithId(query, params)
                .thenApply(response -> {
                    logger.info(
                            "Search with ID completed for query: {}, provider: {}, ID: {}",
                            query,
                            braveSearchProvider.getProviderName(),
                            response.getUniqueId());

                    // For this endpoint, we want to return the ObjectWithUniqueID directly
                    // So we create a custom ResponseEntity with the ETag headers
                    if (eTag != null && (eTag.contains(response.getUniqueId()) || response.getUniqueId().contains(eTag))) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_MODIFIED)
                                .<ObjectWithUniqueID<WebSearchApiResponse>>build();
                    }

                    return ResponseEntity.ok()
                            .header("ETag", "W/" + response.getUniqueId())
                            .header("Cache-Control", "max-age=300, must-revalidate")
                            .header("x-frame-options", "SAMEORIGIN")
                            .header("X-Frame-Options", "SAMEORIGIN")
                            .body(response);
                })
                .exceptionally(ex -> {
                    logger.error("Error performing search with ID: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().build();
                });
    }
}
