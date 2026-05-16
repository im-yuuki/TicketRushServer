package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    public static final String PRESIGNED_URL_CACHE = "s3PresignedUrls";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    private String bucketName;

    @Value("${app.s3.presigned-url-duration}")
    private Duration presignedUrlDuration;

    /**
     * Upload raw bytes to S3 and return the S3 object key.
     *
     * @param data The raw byte data to upload
     * @param key The S3 object key (path) to store the data under
     * @param contentType The MIME type of the data (e.g., "image/png")
     * @return The S3 object key where the data was stored
     * @throws S3Exception If an error occurs during the upload process
     */
    @CacheEvict(cacheNames = PRESIGNED_URL_CACHE, key = "#key", condition = "#key != null && !#key.isEmpty()")
    public String upload(byte[] data, String key, String contentType) throws S3Exception {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                // Server-side encryption
                .serverSideEncryption(ServerSideEncryption.AES256)
                // Cache-Control for CDN
                .cacheControl("public, max-age=31536000")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        log.info("Uploaded to S3: bucket={} key={}", bucketName, key);
        return key;
    }

    /**
     * Delete an object from S3.
     *
     * @param key The S3 object key to delete
     * @throws S3Exception If an error occurs during the deletion process
     */
    @CacheEvict(cacheNames = PRESIGNED_URL_CACHE, key = "#key", condition = "#key != null && !#key.isEmpty()")
    public void delete(String key) throws S3Exception {
        s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        log.info("Deleted S3 object: key={}", key);
    }

    /**
     * Generate a time-limited pre-signed GET URL.
     *
     * @param key The S3 object key to generate the URL for
     * @return A pre-signed URL that can be used to access the object
     */
    @Nullable
    @Cacheable(cacheNames = PRESIGNED_URL_CACHE, key = "#key", condition = "#key != null && !#key.isEmpty()", unless = "#result == null")
    public String generatePresignedUrl(String key) {
        if (key == null) return null;
        if (key.isEmpty()) return null;
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlDuration)
                .getObjectRequest(b -> b.bucket(bucketName).key(key))
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    /**
     * Check if an object exists in S3.
     *
     * @param key The S3 object key to check for existence
     * @return true if the object exists, false otherwise
     */
    public boolean exists(String key) {
        try {
            s3Client.headObject(b -> b.bucket(bucketName).key(key));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

}
