package io.j13n.core.model.llm;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LLMExtractionResult {
    private String extractedValue;
    private double confidenceScore;
    private String reasoning;
    private Map<String, String> metadata;

    public LLMExtractionResult() {
        this.metadata = new HashMap<>();
        this.confidenceScore = 0.0;
    }
}
