package io.j13n.search.controller;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class MapParameterTestController {

    @GetMapping("/map")
    public Map<String, String> testMapParameter(Map<String, String> params) {
        // Simply return the map to verify it's populated with all request parameters
        return params;
    }

    @GetMapping("/multimap")
    public MultiValueMap<String, String> testMultiValueMapParameter(MultiValueMap<String, String> params) {
        // Simply return the multimap to verify it's populated with all request parameters
        return params;
    }
}
