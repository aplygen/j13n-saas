package io.j13n.core.model.analysis;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentAnalysisResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 8693116789124396873L;

    private String sourceId;
    private String sourceType;
    private Map<String, FieldExtraction> extractedFields;
    private LocalDateTime analyzedAt;
    private boolean successful;
    private String errorMessage;

    public DocumentAnalysisResult() {
        this.extractedFields = new HashMap<>();
        this.analyzedAt = LocalDateTime.now();
        this.successful = true;
    }

    @Data
    @Accessors(chain = true)
    public static class FieldExtraction {
        private String fieldName;
        private String extractedValue;
        private double confidenceScore;
        private String reasoning;
        private Map<String, String> metadata;

        public FieldExtraction() {
            this.metadata = new HashMap<>();
            this.confidenceScore = 0.0;
        }
    }

    public Map<String, FieldExtraction> getExtractedFields() {
        if (extractedFields == null) {
            extractedFields = new HashMap<>();
        }
        return extractedFields;
    }
}
