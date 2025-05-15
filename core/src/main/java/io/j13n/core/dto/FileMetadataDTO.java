package io.j13n.core.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDTO {
    private String id;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long size;
    private String bucketName;
    private String path;
    private String provider; // e.g., "aws", "gcp", "azure"
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String url;
    private String status; // e.g., "PENDING", "UPLOADED", "FAILED"
}