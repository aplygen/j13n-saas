package io.j13n.search.controller;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Example controller demonstrating how to use Map and MultiValueMap parameters
 * that are automatically populated with all request parameters.
 */
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    /**
     * Example method that uses a Map<String, String> parameter without specifying a parameter name.
     * The map will be automatically populated with all request parameters.
     * 
     * @param allParams Map containing all request parameters (first value for each parameter)
     * @return The map of all parameters
     */
    @GetMapping("/map")
    public Map<String, String> exampleWithMap(Map<String, String> allParams) {
        // The map is automatically populated with all request parameters
        return allParams;
    }

    /**
     * Example method that uses a MultiValueMap<String, String> parameter without specifying a parameter name.
     * The multimap will be automatically populated with all request parameters, including multiple values for the same parameter.
     * 
     * @param allParams MultiValueMap containing all request parameters (all values for each parameter)
     * @return The multimap of all parameters
     */
    @GetMapping("/multimap")
    public MultiValueMap<String, String> exampleWithMultiValueMap(MultiValueMap<String, String> allParams) {
        // The multimap is automatically populated with all request parameters
        return allParams;
    }

    /**
     * Example method that uses both specific parameters and a map for all other parameters.
     * 
     * @param id Specific parameter that will be extracted from the request
     * @param allParams Map containing all other request parameters
     * @return The map of all parameters
     */
    @GetMapping("/mixed")
    public Map<String, String> exampleWithMixed(@RequestParam String id, Map<String, String> allParams) {
        // The 'id' parameter is handled separately, and all other parameters are in the map
        // Note: The 'id' parameter will also be included in the map
        return allParams;
    }
}
