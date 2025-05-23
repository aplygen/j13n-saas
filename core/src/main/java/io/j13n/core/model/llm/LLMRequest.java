package io.j13n.core.model.llm;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LLMRequest {
    private String prompt;
    private int maxTokens;
    private double temperature;
    private Map<String, Object> options;

    public LLMRequest() {
        this.options = new HashMap<>();
        this.maxTokens = 1000;
        this.temperature = 0.7;
    }
}
