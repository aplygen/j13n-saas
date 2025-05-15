package io.j13n.core.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.j13n.core.enums.FileSystemType;
import io.j13n.core.enums.FileType;
import io.j13n.core.enums.UserFileType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileSystemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8318466191600579113L;

    // Database fields
    private Long id;
    private FileSystemType type;  // STATIC, SECURED
    private String code;          // Client code
    private String name;          // Name of the file
    private FileType fileType;    // FILE, DIRECTORY
    private Long size;
    private Long parentId;
    private UserFileType userFileType;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    // Additional fields for S3 integration
    private String path;          // Full path in S3
    private String url;           // S3 URL
    private String contentType;   // MIME type
    private String bucketName;    // S3 bucket name

    public boolean isDirectory() {
        return FileType.DIRECTORY.equals(this.fileType);
    }

    public String getFullPath() {
        return path == null ? null : path.replace("//", "/");
    }

    public String getUrl() {
        return url == null ? null : url.replace("//", "/");
    }
}
