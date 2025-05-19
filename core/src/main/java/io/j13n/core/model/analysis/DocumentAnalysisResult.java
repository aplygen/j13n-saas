package io.j13n.core.model.analysis;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentAnalysisResult {
    private String sourceId;  // ID of the document or scraping result
    private String sourceType;  // "DOCUMENT" or "SCRAPING_RESULT"
    private Map<String, FieldExtraction> extractedFields;  // Map of field name to extraction result
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
        private double confidenceScore;  // 0.0 to 1.0
        private String reasoning;  // LLM's explanation for the extraction
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