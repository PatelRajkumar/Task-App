package com.pm.taskapp.attachment.service;

import java.time.Duration;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations.
 * Provides abstraction over different storage implementations (S3, Local filesystem).
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@link S3FileStorageService} - AWS S3 storage (production)</li>
 *   <li>{@link LocalFileStorageService} - Local filesystem (development/fallback)</li>
 * </ul>
 *
 * <p>Strategy Pattern allows switching storage backends via configuration
 * without changing business logic in AttachmentService.
 *
 * @since 1.0.0
 */
public interface FileStorageService {

    /**
     * Uploads a file to storage.
     *
     * @param file     MultipartFile to upload
     * @param storageKey Unique storage key/path (e.g., "attachments/project-id/issue-id/uuid-filename")
     * @return Storage path where file was saved
     * @throws com.pm.taskapp.attachment.exception.FileStorageException if upload fails
     */
    String uploadFile(MultipartFile file, String storageKey);

    /**
     * Generates a temporary download URL for a stored file.
     * For S3: generates presigned URL with expiration
     * For Local: generates application URL to download endpoint
     *
     * @param storageKey Storage key/path of the file
     * @param expiration Duration for URL validity
     * @return Temporary download URL
     * @throws com.pm.taskapp.attachment.exception.FileStorageException if generation fails
     */
    String generateDownloadUrl(String storageKey, Duration expiration);

    /**
     * Deletes a file from storage.
     * Note: Soft delete in DB is preferred, this is for physical cleanup.
     *
     * @param storageKey Storage key/path of the file
     * @throws com.pm.taskapp.attachment.exception.FileStorageException if deletion fails
     */
    void deleteFile(String storageKey);

    /**
     * Checks if a file exists in storage.
     *
     * @param storageKey Storage key/path to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String storageKey);

    /**
     * Gets the storage type name (for logging/debugging).
     *
     * @return Storage type (e.g., "S3", "LOCAL")
     */
    String getStorageType();
}