package io.j13n.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.web.search.WebSearchResults;
import io.j13n.core.service.search.TavilySearchService;

@RestController
@RequestMapping("/api/v1/tavily")
public class TavilySearchController {

    private final TavilySearchService tavilySearchService;

    public TavilySearchController(TavilySearchService tavilySearchService) {
        this.tavilySearchService = tavilySearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<WebSearchResults> search(@RequestParam String query) {
        WebSearchResults results = tavilySearchService.search(query);
        return ResponseEntity.ok(results);
    }
}