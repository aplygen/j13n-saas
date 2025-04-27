package io.j13n.search.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasEntry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MapParameterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMapParameter() throws Exception {
        // Test that Map<String, String> parameter is populated with all request parameters
        mockMvc.perform(get("/api/test/map")
                .param("param1", "value1")
                .param("param2", "value2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.param1").value("value1"))
                .andExpect(jsonPath("$.param2").value("value2"));
    }

    @Test
    public void testMultiValueMapParameter() throws Exception {
        // Test that MultiValueMap<String, String> parameter is populated with all request parameters
        mockMvc.perform(get("/api/test/multimap")
                .param("param1", "value1")
                .param("param2", "value2")
                .param("param2", "value3")) // Add a second value for param2
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.param1[0]").value("value1"))
                .andExpect(jsonPath("$.param2[0]").value("value2"))
                .andExpect(jsonPath("$.param2[1]").value("value3"));
    }
}
