package io.j13n.core.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import io.j13n.core.model.llm.LLMExtractionResult;
import io.j13n.core.model.scrape.FormField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public CompletableFuture<Map<String, LLMExtractionResult>> analyzeContent(
            String content, List<FormField> formFields) {
        try {
            var prompt = buildExtractionPrompt(content, formFields);
            Response<AiMessage> response = chatModel.chat(UserMessage.from(prompt));
            String jsonResponse = response.content().text();

            // Parse the JSON response into a map of field names to extraction results
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> rawResults = objectMapper.readValue(jsonResponse, Map.class);
            Map<String, LLMExtractionResult> results = new HashMap<>();

            for (var entry : rawResults.entrySet()) {
                var fieldName = entry.getKey();
                var fieldData = entry.getValue();

                var result = new LLMExtractionResult()
                        .setExtractedValue((String) fieldData.get("value"))
                        .setConfidenceScore(((Number) fieldData.get("confidence")).doubleValue())
                        .setReasoning((String) fieldData.get("reasoning"));

                results.put(fieldName, result);
            }

            return CompletableFuture.completedFuture(results);
        } catch (Exception e) {
            log.error("Failed to analyze content with LLM", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private String buildExtractionPrompt(String content, List<FormField> formFields) {
        var promptBuilder = new StringBuilder();
        promptBuilder.append("Please analyze the following text and extract values for the specified form fields.\n\n");
        promptBuilder.append("Text content:\n").append(content).append("\n\n");
        promptBuilder.append("Form fields to extract:\n");

        for (FormField field : formFields) {
            promptBuilder
                    .append("- ")
                    .append(field.getName())
                    .append(" (")
                    .append(field.getType())
                    .append(")")
                    .append(field.isRequired() ? " [Required]" : "")
                    .append("\n  Label: ")
                    .append(field.getLabel())
                    .append("\n  Description: ")
                    .append(field.getMetadata().getOrDefault("description", "Not provided"))
                    .append("\n\n");
        }

        promptBuilder.append("\nFor each field, please provide:\n");
        promptBuilder.append("1. The extracted value in the 'value' field\n");
        promptBuilder.append("2. A confidence score (0.0 to 1.0) in the 'confidence' field\n");
        promptBuilder.append("3. Your reasoning in the 'reasoning' field\n");
        promptBuilder.append(
                "\nPlease format your response as a JSON object where each key is the field name and the value is an object containing 'value', 'confidence', and 'reasoning' fields.");
        promptBuilder.append("\n\nExample format:\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"fieldName\": {\n");
        promptBuilder.append("    \"value\": \"extracted value\",\n");
        promptBuilder.append("    \"confidence\": 0.95,\n");
        promptBuilder.append("    \"reasoning\": \"Found exact match in paragraph 2\"\n");
        promptBuilder.append("  }\n");
        promptBuilder.append("}");

        return promptBuilder.toString();
    }
}
