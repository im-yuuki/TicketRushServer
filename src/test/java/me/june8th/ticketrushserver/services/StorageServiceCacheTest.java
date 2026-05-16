package me.june8th.ticketrushserver.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StorageServiceCacheTest.TestConfig.class)
@TestPropertySource(properties = {
        "app.s3.bucket=test-bucket",
        "app.s3.presigned-url-duration=PT1H"
})
class StorageServiceCacheTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        cacheManager.getCache(StorageService.PRESIGNED_URL_CACHE).clear();
        reset(s3Client, s3Presigner);
    }

    @Test
    void generatePresignedUrl_shouldReuseCachedValue() {
        String key = "avatars/user.png";
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest("https://cdn.example.com/first"));

        String first = storageService.generatePresignedUrl(key);
        String second = storageService.generatePresignedUrl(key);

        assertEquals("https://cdn.example.com/first", first);
        assertEquals(first, second);
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void delete_shouldEvictCachedPresignedUrl() {
        String key = "avatars/user.png";
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest("https://cdn.example.com/first"))
                .thenReturn(mockPresignedRequest("https://cdn.example.com/second"));

        String first = storageService.generatePresignedUrl(key);
        storageService.delete(key);
        String second = storageService.generatePresignedUrl(key);

        assertEquals("https://cdn.example.com/first", first);
        assertEquals("https://cdn.example.com/second", second);
        verify(s3Presigner, times(2)).presignGetObject(any(GetObjectPresignRequest.class));
        verify(s3Client).deleteObject(anyDeleteObjectRequestBuilder());
    }

    @Test
    void upload_shouldEvictCachedPresignedUrl() {
        String key = "avatars/user.png";
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest("https://cdn.example.com/first"))
                .thenReturn(mockPresignedRequest("https://cdn.example.com/second"));

        String first = storageService.generatePresignedUrl(key);
        storageService.upload(new byte[]{1, 2, 3}, key, "image/png");
        String second = storageService.generatePresignedUrl(key);

        assertEquals("https://cdn.example.com/first", first);
        assertEquals("https://cdn.example.com/second", second);
        verify(s3Presigner, times(2)).presignGetObject(any(GetObjectPresignRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    private PresignedGetObjectRequest mockPresignedRequest(String url) {
        URI uri = URI.create(url);
        return PresignedGetObjectRequest.builder()
                .expiration(Instant.now().plusSeconds(3600))
                .isBrowserExecutable(true)
                .signedHeaders(Map.of("host", List.of(uri.getHost())))
                .httpRequest(SdkHttpRequest.builder()
                        .method(SdkHttpMethod.GET)
                        .uri(uri)
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Consumer<DeleteObjectRequest.Builder> anyDeleteObjectRequestBuilder() {
        return any(Consumer.class);
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        static ConversionService conversionService() {
            return ApplicationConversionService.getSharedInstance();
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(StorageService.PRESIGNED_URL_CACHE);
        }

        @Bean
        S3Client s3Client() {
            return mock(S3Client.class);
        }

        @Bean
        S3Presigner s3Presigner() {
            return mock(S3Presigner.class);
        }

        @Bean
        StorageService storageService(S3Client s3Client, S3Presigner s3Presigner) {
            return new StorageService(s3Client, s3Presigner);
        }
    }
}
