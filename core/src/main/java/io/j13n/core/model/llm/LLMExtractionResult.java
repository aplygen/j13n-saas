package io.j13n.core.model.llm;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LLMExtractionResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 6899645771826994047L;

    private String extractedValue;
    private double confidenceScore;
    private String reasoning;
    private Map<String, String> metadata;

    public LLMExtractionResult() {
        this.metadata = new HashMap<>();
        this.confidenceScore = 0.0;
    }
}
