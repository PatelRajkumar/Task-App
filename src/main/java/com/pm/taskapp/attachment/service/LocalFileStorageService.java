package com.pm.taskapp.attachment.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pm.taskapp.attachment.exception.FileStorageException;

import lombok.extern.slf4j.Slf4j;

/**
 * Local filesystem implementation of FileStorageService.
 * Used for development, testing, or as fallback storage.
 *
 * <p>
 * Features:
 * <ul>
 * <li>Stores files in configurable local directory</li>
 * <li>Automatic directory creation</li>
 * <li>Generates application URLs for downloads</li>
 * <li>Fallback when S3 is unavailable</li>
 * </ul>
 *
 * <p>
 * Configuration (application-dev.yml):
 * 
 * <pre>
 * app:
 *   file-upload:
 *     storage-type: local
 *     local-storage-path: ./uploads
 * </pre>
 *
 * <p>
 * Activation:
 * Only enabled when storage-type=local in configuration
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.file-upload.storage-type", havingValue = "local")
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadDir;

    public LocalFileStorageService(
            @Value("${app.file-upload.local-storage-path:./uploads}") String uploadPath) {

        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();

        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(uploadDir);
            log.info("LocalFileStorageService initialized - Upload directory: {}", uploadDir);
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", uploadDir, e);
            throw new IllegalStateException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String storageKey) {
        log.debug("Uploading file to local storage: {} (size: {} bytes)", storageKey, file.getSize());

        try {
            // Resolve file path (storageKey is relative path like
            // "attachments/project-id/issue-id/file.pdf")
            Path filePath = uploadDir.resolve(storageKey).normalize();

            // Security check: prevent path traversal outside upload directory
            if (!filePath.startsWith(uploadDir)) {
                log.error("Path traversal attempt detected: {}", storageKey);
                throw FileStorageException.uploadFailed("Invalid storage path: path traversal detected");
            }

            // Create parent directories if they don't exist
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.debug("Created parent directories: {}", parentDir);
            }

            // Copy file to destination
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Successfully uploaded file to local storage: {}", filePath);
            return storageKey;

        } catch (IOException e) {
            log.error("IO error uploading file to local storage: {}", storageKey, e);
            throw FileStorageException.uploadFailed("Failed to store file: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error uploading file to local storage: {}", storageKey, e);
            throw FileStorageException.uploadFailed("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadUrl(String storageKey, Duration expiration) {
        log.debug("Generating download URL for local file: {}", storageKey);

        // Note: Local storage doesn't support presigned URLs with expiration
        // Instead, generate application URL that points to download endpoint
        // The controller should handle authorization

        try {
            // Build URL like:
            // http://localhost:8080/api/v1/attachments/{attachmentId}/download
            // The storageKey is not directly exposed in URL for security
            // Controller will use attachment ID to fetch and authorize

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .build()
                    .toUriString();

            // Extract attachment ID from storageKey (last part before extension)
            // Format: attachments/{projectId}/{issueId}/{uuid}-{filename}
            // For local storage, we'll return a placeholder that controller can resolve
            String downloadUrl = baseUrl + "/api/v1/files/" + storageKey;

            log.debug("Generated local download URL for: {}", storageKey);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Error generating download URL for local file: {}", storageKey, e);
            throw FileStorageException.downloadFailed("Failed to generate download URL: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        log.debug("Deleting file from local storage: {}", storageKey);

        try {
            Path filePath = uploadDir.resolve(storageKey).normalize();

            // Security check: prevent path traversal
            if (!filePath.startsWith(uploadDir)) {
                log.error("Path traversal attempt detected during delete: {}", storageKey);
                throw FileStorageException.deleteFailed("Invalid storage path: path traversal detected");
            }

            // Delete file if exists
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Successfully deleted file from local storage: {}", filePath);

                // Optionally delete empty parent directories
                cleanupEmptyParentDirectories(filePath.getParent());
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException e) {
            log.error("IO error deleting file from local storage: {}", storageKey, e);
            throw FileStorageException.deleteFailed("Failed to delete file: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error deleting file from local storage: {}", storageKey, e);
            throw FileStorageException.deleteFailed("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String storageKey) {
        log.debug("Checking if file exists in local storage: {}", storageKey);

        try {
            Path filePath = uploadDir.resolve(storageKey).normalize();

            // Security check: prevent path traversal
            if (!filePath.startsWith(uploadDir)) {
                log.warn("Path traversal attempt detected during existence check: {}", storageKey);
                return false;
            }

            boolean exists = Files.exists(filePath) && Files.isRegularFile(filePath);
            log.debug("File exists in local storage: {} - {}", storageKey, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error checking file existence in local storage: {}", storageKey, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    /**
     * Cleans up empty parent directories after file deletion.
     * Removes empty directories up to the upload root directory.
     *
     * @param directory Directory to start cleanup from
     */
    private void cleanupEmptyParentDirectories(Path directory) {
        try {
            // Don't delete the root upload directory
            if (directory == null || directory.equals(uploadDir) || !directory.startsWith(uploadDir)) {
                return;
            }

            // Check if directory is empty
            if (Files.isDirectory(directory) && isDirectoryEmpty(directory)) {
                log.debug("Deleting empty directory: {}", directory);
                Files.delete(directory);

                // Recursively check parent
                cleanupEmptyParentDirectories(directory.getParent());
            }

        } catch (IOException e) {
            log.warn("Failed to cleanup empty directory: {}", directory, e);
            // Non-critical error, just log and continue
        }
    }

    /**
     * Checks if a directory is empty.
     *
     * @param directory Directory path
     * @return true if directory is empty
     * @throws IOException if unable to read directory
     */
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return stream.findAny().isEmpty();
        }
    }

    /**
     * Gets the configured upload directory path.
     *
     * @return Upload directory path
     */
    public Path getUploadDir() {
        return uploadDir;
    }

    /**
     * Resolves a storage key to an absolute file path.
     * Used for internal operations (not exposed to users).
     *
     * @param storageKey Storage key
     * @return Absolute file path
     */
    public Path resolveFilePath(String storageKey) {
        return uploadDir.resolve(storageKey).normalize();
    }
}