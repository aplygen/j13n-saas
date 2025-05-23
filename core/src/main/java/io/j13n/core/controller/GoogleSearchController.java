package io.j13n.core.controller;

import io.j13n.core.model.JobSearchResult;
import io.j13n.core.service.search.google.GoogleJobSearchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/google/jobs")
public class GoogleSearchController {

    private final GoogleJobSearchService googleJobSearchService;

    public GoogleSearchController(GoogleJobSearchService googleJobSearchService) {
        this.googleJobSearchService = googleJobSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobSearchResult>> searchJobs(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "false") boolean remoteOnly) {

        List<JobSearchResult> results = googleJobSearchService.searchJobs(query, location, remoteOnly);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/remote")
    public ResponseEntity<List<JobSearchResult>> searchRemoteJobs(
            @RequestParam String query, @RequestParam(required = false) String location) {

        List<JobSearchResult> results = googleJobSearchService.searchJobs(query, location, true);
        return ResponseEntity.ok(results);
    }
}
