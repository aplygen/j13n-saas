package io.j13n.core.service.file;

import io.j13n.core.model.file.FileDetail;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FileSystemService implements AutoCloseable {

    private static final String FILE_SEPARATOR = "/";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours
    private final S3Client s3Client;
    private final String bucketName;

    @Getter
    private final Path tempFolder;
    private final Map<String, Long> fileAccessCache = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    public FileSystemService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        try {
            this.tempFolder = Files.createTempDirectory("download-" + this.bucketName);
            startCacheCleanupThread();
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp directory", e);
        }
    }

    private void startCacheCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    cleanupExpiredFiles();
                    Thread.sleep(3600000); // Run every hour
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("TempFolder-Cleanup-Thread");
        cleanupThread.start();
    }

    private void cleanupExpiredFiles() {
        long now = System.currentTimeMillis();
        fileAccessCache.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > CACHE_EXPIRY_MS) {
                try {
                    Path filePath = tempFolder.resolve(entry.getKey());
                    Files.deleteIfExists(filePath);
                    return true;
                } catch (IOException e) {
                    logger.error("Failed to delete expired file: {}", entry.getKey(), e);
                }
            }
            return false;
        });
    }

    public boolean exists(String path) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(path).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public Page<FileDetail> list(String path, Pageable pageable) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(path)
                .delimiter(FILE_SEPARATOR)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<FileDetail> files = new ArrayList<>();

        response.commonPrefixes().forEach(prefix -> {
            FileDetail dir = new FileDetail()
                    .setName(getNameFromPath(prefix.prefix()))
                    .setDirectory(true)
                    .setFilePath(prefix.prefix())
                    .setCreatedDate(Instant.now().toEpochMilli());
            files.add(dir);
        });

        response.contents().forEach(content -> {
            if (!content.key().endsWith(FILE_SEPARATOR)) {
                FileDetail file = new FileDetail()
                        .setName(getNameFromPath(content.key()))
                        .setDirectory(false)
                        .setSize(content.size())
                        .setFilePath(content.key())
                        .setUrl(generateUrl(content.key()))
                        .setLastModifiedTime(content.lastModified().toEpochMilli());
                files.add(file);
            }
        });

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), files.size());

        return new PageImpl<>(files.subList(start, end), pageable, files.size());
    }

    public FileDetail getFileDetail(String path) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(path).build());

            return new FileDetail()
                    .setName(getNameFromPath(path))
                    .setDirectory(false)
                    .setSize(response.contentLength())
                    .setFilePath(path)
                    .setUrl(generateUrl(path))
                    .setLastModifiedTime(response.lastModified().toEpochMilli());
        } catch (NoSuchKeyException e) {
            return null;
        }
    }

    public File downloadFile(String path) throws IOException {
        String hashedPath = hashPath(path);
        Path filePath = createTempFilePath(hashedPath);

        if (Files.exists(filePath)) {
            Long lastAccess = fileAccessCache.get(hashedPath);
            if (lastAccess != null && System.currentTimeMillis() - lastAccess < CACHE_EXPIRY_MS) {
                fileAccessCache.put(hashedPath, System.currentTimeMillis());
                return filePath.toFile();
            }
        }

        try {
            GetObjectRequest request =
                    GetObjectRequest.builder().bucket(bucketName).key(path).build();

            s3Client.getObject(request, filePath);
            fileAccessCache.put(hashedPath, System.currentTimeMillis());
            return filePath.toFile();
        } catch (Exception e) {
            Files.deleteIfExists(filePath);
            throw new IOException("Failed to download file: " + path, e);
        }
    }

    public FileDetail uploadFile(MultipartFile file, String path, boolean override) throws IOException {
        String key = path + FILE_SEPARATOR + file.getOriginalFilename();

        if (!override && exists(key)) {
            throw new IllegalStateException("File already exists: " + key);
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = URLConnection.guessContentTypeFromName(file.getOriginalFilename());
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return new FileDetail()
                .setName(file.getOriginalFilename())
                .setDirectory(false)
                .setSize(file.getSize())
                .setFilePath(key)
                .setUrl(generateUrl(key))
                .setLastModifiedTime(System.currentTimeMillis());
    }

    public boolean deleteFile(String path) {
        try {
            if (path.endsWith(FILE_SEPARATOR)) {
                ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .build();

                ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
                List<ObjectIdentifier> objects = listResponse.contents().stream()
                        .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                        .collect(Collectors.toList());

                if (!objects.isEmpty()) {
                    DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder().objects(objects).build())
                            .build();

                    s3Client.deleteObjects(deleteRequest);
                }
            } else {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .build();

                s3Client.deleteObject(deleteRequest);
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete file/directory: " + path, e);
            return false;
        }
    }

    public FileDetail createDirectory(String path) {
        String directoryPath = path.endsWith(FILE_SEPARATOR) ? path : path + FILE_SEPARATOR;

        PutObjectRequest request =
                PutObjectRequest.builder().bucket(bucketName).key(directoryPath).build();

        s3Client.putObject(request, RequestBody.empty());

        return new FileDetail()
                .setName(getNameFromPath(path))
                .setDirectory(true)
                .setFilePath(directoryPath)
                .setCreatedDate(System.currentTimeMillis());
    }

    private String getNameFromPath(String path) {
        if (path.endsWith(FILE_SEPARATOR)) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSeparator = path.lastIndexOf(FILE_SEPARATOR);
        return lastSeparator >= 0 ? path.substring(lastSeparator + 1) : path;
    }

    private String generateUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    private Path createTempFilePath(String path) throws IOException {
        String hashedPath = hashPath(path);
        Path filePath = tempFolder.resolve(hashedPath);
        Files.createDirectories(filePath.getParent());
        return filePath;
    }

    private String hashPath(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(path.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(path.hashCode());
        }
    }

    @Override
    public void close() {
        try {
            cleanupExpiredFiles();
            FileSystemUtils.deleteRecursively(tempFolder);
        } catch (IOException e) {
            logger.error("Failed to cleanup temp folder: {}", tempFolder, e);
        }
    }
}
