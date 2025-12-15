package com.pm.taskapp.attachment.service;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pm.taskapp.attachment.exception.FileStorageException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * AWS S3 implementation of FileStorageService.
 * Handles file uploads, presigned URL generation, and deletions.
 *
 * <p>
 * Features:
 * <ul>
 * <li>Production S3 storage with proper error handling</li>
 * <li>Presigned URLs for secure, temporary downloads</li>
 * <li>LocalStack support for local development</li>
 * <li>Configurable endpoint override</li>
 * </ul>
 *
 * <p>
 * Configuration (application-dev.yml):
 * 
 * <pre>
 * aws:
 *   s3:
 *     region: eu-north-1
 *     bucket-name: taskapp-attachments-dev
 *     access-key: ${AWS_ACCESS_KEY}
 *     secret-key: ${AWS_SECRET_KEY}
 *     endpoint-override: http://localhost:4566  # LocalStack (optional)
 *     path-style-access: true  # Required for LocalStack
 *     presigned-url-expiration-hours: 1
 * </pre>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@Primary // Use S3 as primary storage (override with @ConditionalOnProperty for local)
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final Duration presignedUrlExpiration;

    public S3FileStorageService(
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.endpoint-override:}") String endpointOverride,
            @Value("${aws.s3.path-style-access:false}") boolean pathStyleAccess,
            @Value("${aws.s3.presigned-url-expiration-hours:1}") int expirationHours) {

        this.bucketName = bucketName;
        this.presignedUrlExpiration = Duration.ofHours(expirationHours);

        // Build AWS credentials
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);

        // Build S3 Client
        var s3ClientBuilder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        // Configure endpoint override (for LocalStack)
        if (endpointOverride != null && !endpointOverride.trim().isEmpty()) {
            log.info("Using S3 endpoint override: {}", endpointOverride);
            s3ClientBuilder.endpointOverride(URI.create(endpointOverride));
        }

        // Enable path-style access (required for LocalStack)
        if (pathStyleAccess) {
            log.info("Enabling path-style access for S3");
            s3ClientBuilder.forcePathStyle(true);
        }

        this.s3Client = s3ClientBuilder.build();

        // Build S3 Presigner (for download URLs)
        var presignerBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        if (endpointOverride != null && !endpointOverride.trim().isEmpty()) {
            presignerBuilder.endpointOverride(URI.create(endpointOverride));
        }
        if (pathStyleAccess) {
            presignerBuilder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        this.s3Presigner = presignerBuilder.build();

        log.info("S3FileStorageService initialized - Bucket: {}, Region: {}, Expiration: {} hours",
                bucketName, region, expirationHours);
    }

    @Override
    public String uploadFile(MultipartFile file, String storageKey) {
        log.debug("Uploading file to S3: {} (size: {} bytes)", storageKey, file.getSize());

        try {
            // Build PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Upload file to S3
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded file to S3: {}", storageKey);
            return storageKey;

        } catch (S3Exception e) {
            log.error("S3 error uploading file: {} - Code: {}, Message: {}",
                    storageKey, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.s3Error("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);

        } catch (IOException e) {
            log.error("IO error reading file for upload: {}", storageKey, e);
            throw FileStorageException.uploadFailed("Failed to read file: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error uploading file to S3: {}", storageKey, e);
            throw FileStorageException.uploadFailed("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadUrl(String storageKey, Duration expiration) {
        log.debug("Generating presigned URL for: {} (expiration: {})", storageKey, expiration);

        try {
            // Build GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            // Build presign request with expiration
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate presigned URL
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.debug("Generated presigned URL for: {}", storageKey);
            return url;

        } catch (S3Exception e) {
            log.error("S3 error generating presigned URL: {} - Code: {}, Message: {}",
                    storageKey, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.s3Error("Failed to generate download URL: " + e.awsErrorDetails().errorMessage(),
                    e);

        } catch (Exception e) {
            log.error("Unexpected error generating presigned URL: {}", storageKey, e);
            throw FileStorageException.downloadFailed("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        log.debug("Deleting file from S3: {}", storageKey);

        try {
            // Build DeleteObjectRequest
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            // Delete file from S3
            s3Client.deleteObject(deleteObjectRequest);

            log.info("Successfully deleted file from S3: {}", storageKey);

        } catch (S3Exception e) {
            log.error("S3 error deleting file: {} - Code: {}, Message: {}",
                    storageKey, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.s3Error("Failed to delete file: " + e.awsErrorDetails().errorMessage(), e);

        } catch (Exception e) {
            log.error("Unexpected error deleting file from S3: {}", storageKey, e);
            throw FileStorageException.deleteFailed("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String storageKey) {
        log.debug("Checking if file exists in S3: {}", storageKey);

        try {
            // Build HeadObjectRequest (lightweight check without downloading)
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            // Check if object exists
            s3Client.headObject(headObjectRequest);

            log.debug("File exists in S3: {}", storageKey);
            return true;

        } catch (NoSuchKeyException e) {
            log.debug("File does not exist in S3: {}", storageKey);
            return false;

        } catch (S3Exception e) {
            log.warn("S3 error checking file existence: {} - Code: {}",
                    storageKey, e.awsErrorDetails().errorCode());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error checking file existence: {}", storageKey, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "S3";
    }

    /**
     * Gets the configured S3 bucket name.
     *
     * @return Bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Gets the configured presigned URL expiration duration.
     *
     * @return Expiration duration
     */
    public Duration getPresignedUrlExpiration() {
        return presignedUrlExpiration;
    }
}