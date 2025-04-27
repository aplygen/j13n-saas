package io.j13n.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSnakeCaseNamingStrategy() throws IOException {
        // Create a test object with camelCase properties
        TestModel testModel = new TestModel();
        testModel.setPropertyOne("value1");
        testModel.setPropertyTwo(42);
        testModel.setIsActive(true);

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(testModel);
        System.out.println("[DEBUG_LOG] Serialized JSON: " + json);

        // Verify JSON contains snake_case properties
        assertTrue(json.contains("property_one"));
        assertTrue(json.contains("property_two"));
        assertTrue(json.contains("is_active"));

        // Deserialize back to object
        TestModel deserializedModel = objectMapper.readValue(json, TestModel.class);

        // Verify properties were correctly deserialized
        assertEquals("value1", deserializedModel.getPropertyOne());
        assertEquals(42, deserializedModel.getPropertyTwo());
        assertEquals(true, deserializedModel.getIsActive());
    }

    // Test model class without Jackson annotations
    public static class TestModel {
        private String propertyOne;
        private Integer propertyTwo;
        private Boolean isActive;

        public String getPropertyOne() {
            return propertyOne;
        }

        public void setPropertyOne(String propertyOne) {
            this.propertyOne = propertyOne;
        }

        public Integer getPropertyTwo() {
            return propertyTwo;
        }

        public void setPropertyTwo(Integer propertyTwo) {
            this.propertyTwo = propertyTwo;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
}
