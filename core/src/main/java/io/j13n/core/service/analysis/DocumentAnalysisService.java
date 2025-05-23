package io.j13n.core.service.analysis;

import io.j13n.core.model.analysis.DocumentAnalysisResult;
import io.j13n.core.model.analysis.DocumentAnalysisResult.FieldExtraction;
import io.j13n.core.model.file.FileDetail;
import io.j13n.core.model.llm.LLMExtractionResult;
import io.j13n.core.model.scrape.FormField;
import io.j13n.core.model.scrape.JobScrapingResult;
import io.j13n.core.service.llm.LLMService;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAnalysisService {
    private final LLMService llmService;

    public CompletableFuture<DocumentAnalysisResult> analyzeDocument(
            FileDetail fileDetail, List<FormField> formFields) {
        var result = new DocumentAnalysisResult()
                .setSourceId(fileDetail.getId().toString())
                .setSourceType("DOCUMENT");

        try {
            // TODO: Implement document text extraction
            String documentText = ""; // extractTextFromDocument(fileDetail);

            return llmService
                    .analyzeContent(documentText, formFields)
                    .thenApply(extractionResults -> populateAnalysisResult(result, extractionResults))
                    .exceptionally(e -> {
                        log.error("Failed to analyze document: " + fileDetail.getId(), e);
                        return result.setSuccessful(false)
                                .setErrorMessage("Failed to analyze document: " + e.getMessage());
                    });
        } catch (Exception e) {
            log.error("Failed to analyze document: " + fileDetail.getId(), e);
            return CompletableFuture.completedFuture(
                    result.setSuccessful(false).setErrorMessage("Failed to analyze document: " + e.getMessage()));
        }
    }

    public CompletableFuture<DocumentAnalysisResult> analyzeScrapingResult(
            JobScrapingResult scrapingResult, List<FormField> formFields) {
        var result = new DocumentAnalysisResult()
                .setSourceId(scrapingResult.getSourceUrl())
                .setSourceType("SCRAPING_RESULT");

        try {
            String content = scrapingResult.getRawTextContent();
            if (content == null || content.trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        result.setSuccessful(false).setErrorMessage("No text content available in scraping result"));
            }

            return llmService
                    .analyzeContent(content, formFields)
                    .thenApply(extractionResults -> populateAnalysisResult(result, extractionResults))
                    .exceptionally(e -> {
                        log.error("Failed to analyze scraping result: " + scrapingResult.getSourceUrl(), e);
                        return result.setSuccessful(false)
                                .setErrorMessage("Failed to analyze scraping result: " + e.getMessage());
                    });
        } catch (Exception e) {
            log.error("Failed to analyze scraping result: " + scrapingResult.getSourceUrl(), e);
            return CompletableFuture.completedFuture(result.setSuccessful(false)
                    .setErrorMessage("Failed to analyze scraping result: " + e.getMessage()));
        }
    }

    public CompletableFuture<DocumentAnalysisResult> analyzeBothSources(
            FileDetail fileDetail, JobScrapingResult scrapingResult, List<FormField> formFields) {
        CompletableFuture<DocumentAnalysisResult> docFuture = analyzeDocument(fileDetail, formFields);
        CompletableFuture<DocumentAnalysisResult> scrapeFuture = analyzeScrapingResult(scrapingResult, formFields);

        return CompletableFuture.allOf(docFuture, scrapeFuture)
                .thenApply(v -> mergeBestResults(docFuture.join(), scrapeFuture.join()));
    }

    private DocumentAnalysisResult mergeBestResults(
            DocumentAnalysisResult docResult, DocumentAnalysisResult scrapeResult) {
        var mergedResult = new DocumentAnalysisResult()
                .setSourceType("COMBINED")
                .setSourceId("doc:" + docResult.getSourceId() + ",scrape:" + scrapeResult.getSourceId());

        // Merge results, taking the extraction with higher confidence for each field
        var allFields = docResult.getExtractedFields().keySet();
        allFields.addAll(scrapeResult.getExtractedFields().keySet());

        for (String fieldName : allFields) {
            var docExtraction = docResult.getExtractedFields().get(fieldName);
            var scrapeExtraction = scrapeResult.getExtractedFields().get(fieldName);

            if (docExtraction == null) {
                mergedResult.getExtractedFields().put(fieldName, scrapeExtraction);
            } else if (scrapeExtraction == null) {
                mergedResult.getExtractedFields().put(fieldName, docExtraction);
            } else {
                // Take the extraction with higher confidence
                var bestExtraction = docExtraction.getConfidenceScore() > scrapeExtraction.getConfidenceScore()
                        ? docExtraction
                        : scrapeExtraction;
                mergedResult.getExtractedFields().put(fieldName, bestExtraction);
            }
        }

        return mergedResult;
    }

    private DocumentAnalysisResult populateAnalysisResult(
            DocumentAnalysisResult result, Map<String, LLMExtractionResult> extractionResults) {
        for (Entry<String, LLMExtractionResult> entry : extractionResults.entrySet()) {
            LLMExtractionResult llmResult = entry.getValue();
            var fieldExtraction = new FieldExtraction()
                    .setFieldName(entry.getKey())
                    .setExtractedValue(llmResult.getExtractedValue())
                    .setConfidenceScore(llmResult.getConfidenceScore())
                    .setReasoning(llmResult.getReasoning());

            // Copy any additional metadata
            fieldExtraction.getMetadata().putAll(llmResult.getMetadata());

            result.getExtractedFields().put(entry.getKey(), fieldExtraction);
        }

        return result;
    }
}
