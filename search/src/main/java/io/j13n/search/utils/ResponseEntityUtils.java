package io.j13n.search.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.j13n.commons.dto.ObjectWithUniqueID;

import java.util.concurrent.CompletableFuture;

public class ResponseEntityUtils {

    private ResponseEntityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a ResponseEntity with ETag support
     * 
     * @param obj The object with unique ID
     * @param eTag The ETag from the request (If-None-Match header)
     * @param cacheAge The cache age in seconds
     * @param <T> The type of the object
     * @return A Mono with the ResponseEntity
     */
    public static <T> ResponseEntity<T> makeResponseEntity(ObjectWithUniqueID<T> obj, String eTag, int cacheAge) {
        return makeResponseEntity(obj, eTag, cacheAge, null);
    }

    /**
     * Creates a ResponseEntity with ETag support
     * 
     * @param obj The object with unique ID
     * @param eTag The ETag from the request (If-None-Match header)
     * @param cacheAge The cache age in seconds
     * @param contentType The content type of the response
     * @param <T> The type of the object
     * @return A Mono with the ResponseEntity
     */
    public static <T> ResponseEntity<T> makeResponseEntity(
            ObjectWithUniqueID<T> obj, String eTag, int cacheAge, String contentType) {

        if (eTag != null && (eTag.contains(obj.getUniqueId()) || obj.getUniqueId().contains(eTag)))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .build();

        var rp = ResponseEntity.ok()
                .header("ETag", "W/" + obj.getUniqueId())
                .header("Cache-Control", "max-age=" + cacheAge + ", must-revalidate")
                .header("x-frame-options", "SAMEORIGIN")
                .header("X-Frame-Options", "SAMEORIGIN");

        if (contentType != null)
            rp = rp.header("Content-Type", contentType);

        if (obj.getHeaders() != null) {
            obj.getHeaders().forEach(rp::header);
        }

        return rp.body(obj.getObject());
    }

    /**
     * Creates a CompletableFuture of ResponseEntity with ETag support
     * 
     * @param futureObj The future object with unique ID
     * @param eTag The ETag from the request (If-None-Match header)
     * @param cacheAge The cache age in seconds
     * @param <T> The type of the object
     * @return A CompletableFuture with the ResponseEntity
     */
    public static <T> CompletableFuture<ResponseEntity<T>> makeResponseEntityAsync(
            CompletableFuture<ObjectWithUniqueID<T>> futureObj, String eTag, int cacheAge) {
        return makeResponseEntityAsync(futureObj, eTag, cacheAge, null);
    }

    /**
     * Creates a CompletableFuture of ResponseEntity with ETag support
     * 
     * @param futureObj The future object with unique ID
     * @param eTag The ETag from the request (If-None-Match header)
     * @param cacheAge The cache age in seconds
     * @param contentType The content type of the response
     * @param <T> The type of the object
     * @return A CompletableFuture with the ResponseEntity
     */
    public static <T> CompletableFuture<ResponseEntity<T>> makeResponseEntityAsync(
            CompletableFuture<ObjectWithUniqueID<T>> futureObj, String eTag, int cacheAge, String contentType) {
        
        return futureObj.thenApply(obj -> makeResponseEntity(obj, eTag, cacheAge, contentType));
    }
}
