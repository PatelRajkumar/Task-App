package com.pm.taskapp.attachment.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pm.taskapp.attachment.enums.FileCategory;
import com.pm.taskapp.attachment.exception.FileUploadException;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating uploaded files.
 * Performs security checks, size validation, and MIME type verification.
 *
 * <p>Validation Steps:
 * <ol>
 *   <li>Check file is not empty</li>
 *   <li>Validate filename (no path traversal, special chars)</li>
 *   <li>Check file size against configured maximum</li>
 *   <li>Verify MIME type is in whitelist</li>
 *   <li>Detect actual content type using Apache Tika</li>
 * </ol>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileValidationService {

    private final long maxFileSize;
    private final Set<String> allowedMimeTypes;
    private final Tika tika;

    // Regex patterns for security
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[<>:\"/\\\\|?*\\x00-\\x1F]");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("\\.\\.[\\\\/]");
    private static final int MAX_FILENAME_LENGTH = 255;

    public FileValidationService(
            @Value("${app.file-upload.max-file-size}") long maxFileSize,
            @Value("${app.file-upload.allowed-mime-types}") String allowedMimeTypesStr) {
        this.maxFileSize = maxFileSize;
        this.allowedMimeTypes = new HashSet<>(Arrays.asList(allowedMimeTypesStr.split(",")));
        this.tika = new Tika();
        
        log.info("FileValidationService initialized - Max size: {} bytes, Allowed types: {}", 
                maxFileSize, allowedMimeTypes.size());
    }

    /**
     * Validates an uploaded file against all security and business rules.
     *
     * @param file MultipartFile to validate
     * @throws FileUploadException if validation fails
     */
    public void validateFile(MultipartFile file) {
        log.debug("Validating file: {}", file.getOriginalFilename());

        // 1. Check file is not null or empty
        if (file == null || file.isEmpty()) {
            log.warn("File validation failed: file is empty");
            throw FileUploadException.emptyFile();
        }

        // 2. Validate filename
        String originalFilename = file.getOriginalFilename();
        validateFilename(originalFilename);

        // 3. Validate file size
        validateFileSize(file.getSize());

        // 4. Validate MIME type (declared)
        String declaredMimeType = file.getContentType();
        validateMimeType(declaredMimeType);

        // 5. Detect actual MIME type and verify (security check)
        String detectedMimeType = detectMimeType(file);
        validateMimeTypeMatch(declaredMimeType, detectedMimeType);

        log.debug("File validation passed: {} ({} bytes, type: {})", 
                originalFilename, file.getSize(), declaredMimeType);
    }

    /**
     * Validates filename for security issues.
     * Prevents path traversal, null bytes, and invalid characters.
     *
     * @param filename Original filename
     * @throws FileUploadException if filename is invalid
     */
    public void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw FileUploadException.invalidFilename("Filename cannot be null or empty");
        }

        // Check for path traversal attempts
        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            log.warn("Path traversal attempt detected in filename: {}", filename);
            throw FileUploadException.invalidFilename("Filename contains path traversal characters");
        }

        // Check for invalid characters (OS-specific special chars)
        if (INVALID_FILENAME_PATTERN.matcher(filename).find()) {
            log.warn("Invalid characters detected in filename: {}", filename);
            throw FileUploadException.invalidFilename("Filename contains invalid characters");
        }

        // Check filename length
        if (filename.length() > MAX_FILENAME_LENGTH) {
            log.warn("Filename too long: {} characters", filename.length());
            throw FileUploadException.invalidFilename(
                    "Filename exceeds maximum length of " + MAX_FILENAME_LENGTH + " characters");
        }

        // Check for hidden files (starting with dot) - optional, can be removed if needed
        if (filename.startsWith(".")) {
            log.warn("Hidden file detected: {}", filename);
            throw FileUploadException.invalidFilename("Hidden files are not allowed");
        }
    }

    /**
     * Validates file size against configured maximum.
     *
     * @param fileSize Size in bytes
     * @throws FileUploadException if file is too large
     */
    public void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw FileUploadException.emptyFile();
        }

        if (fileSize > maxFileSize) {
            long maxSizeMB = maxFileSize / (1024 * 1024);
            log.warn("File size {} bytes exceeds maximum {} bytes ({} MB)", 
                    fileSize, maxFileSize, maxSizeMB);
            throw FileUploadException.fileTooLarge(maxSizeMB);
        }
    }

    /**
     * Validates MIME type against whitelist.
     *
     * @param mimeType MIME type to validate
     * @throws FileUploadException if MIME type is not allowed
     */
    public void validateMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            log.warn("MIME type is null or empty");
            throw FileUploadException.invalidMimeType("Unknown");
        }

        // Normalize MIME type (lowercase, trim)
        String normalizedMimeType = mimeType.toLowerCase().trim();

        if (!allowedMimeTypes.contains(normalizedMimeType)) {
            log.warn("MIME type not allowed: {}", mimeType);
            throw FileUploadException.invalidMimeType(mimeType);
        }
    }

    /**
     * Detects actual MIME type of file using Apache Tika.
     * This is a security measure to prevent MIME type spoofing.
     *
     * @param file MultipartFile to analyze
     * @return Detected MIME type
     * @throws FileUploadException if detection fails
     */
    public String detectMimeType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String detectedType = tika.detect(inputStream, file.getOriginalFilename());
            log.debug("Detected MIME type: {} for file: {}", detectedType, file.getOriginalFilename());
            return detectedType;
        } catch (IOException e) {
            log.error("Failed to detect MIME type for file: {}", file.getOriginalFilename(), e);
            throw FileUploadException.invalidMimeType("Could not detect file type");
        }
    }

    /**
     * Validates that declared MIME type matches detected MIME type.
     * Prevents MIME type spoofing attacks.
     *
     * @param declaredMimeType MIME type from request header
     * @param detectedMimeType MIME type from content analysis
     * @throws FileUploadException if types don't match
     */
    private void validateMimeTypeMatch(String declaredMimeType, String detectedMimeType) {
        if (detectedMimeType == null) {
            return; // Skip if detection failed
        }

        String normalizedDeclared = declaredMimeType.toLowerCase().trim();
        String normalizedDetected = detectedMimeType.toLowerCase().trim();

        // Some MIME types have variations (e.g., image/jpg vs image/jpeg)
        // We'll do a basic check - can be made more sophisticated
        if (!normalizedDeclared.equals(normalizedDetected)) {
            // Allow some common variations
            if (!isMimeTypeVariation(normalizedDeclared, normalizedDetected)) {
                log.warn("MIME type mismatch - Declared: {}, Detected: {}", 
                        declaredMimeType, detectedMimeType);
                throw FileUploadException.invalidMimeType(
                        "File content does not match declared type");
            }
        }
    }

    /**
     * Checks if two MIME types are acceptable variations of each other.
     *
     * @param type1 First MIME type
     * @param type2 Second MIME type
     * @return true if types are acceptable variations
     */
    private boolean isMimeTypeVariation(String type1, String type2) {
        // Handle common MIME type variations
        Set<String> jpegVariations = Set.of("image/jpeg", "image/jpg", "image/pjpeg");
        Set<String> msWordVariations = Set.of(
                "application/msword",
                "application/vnd.ms-word",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        Set<String> excelVariations = Set.of(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        return (jpegVariations.contains(type1) && jpegVariations.contains(type2)) ||
               (msWordVariations.contains(type1) && msWordVariations.contains(type2)) ||
               (excelVariations.contains(type1) && excelVariations.contains(type2));
    }

    /**
     * Detects file category based on MIME type.
     *
     * @param mimeType MIME type
     * @return FileCategory enum
     */
    public FileCategory detectFileCategory(String mimeType) {
        return FileCategory.fromMimeType(mimeType);
    }

    /**
     * Sanitizes filename by removing invalid characters.
     * Used by AttachmentMapper.
     *
     * @param filename Original filename
     * @return Sanitized filename
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed-file";
        }

        // Remove invalid characters
        String sanitized = INVALID_FILENAME_PATTERN.matcher(filename).replaceAll("_");

        // Remove path traversal
        sanitized = PATH_TRAVERSAL_PATTERN.matcher(sanitized).replaceAll("");

        // Trim and ensure not empty
        sanitized = sanitized.trim();
        if (sanitized.isEmpty()) {
            return "unnamed-file";
        }

        // Truncate if too long (leave room for UUID prefix)
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            String extension = getFileExtension(sanitized);
            int maxBaseLength = MAX_FILENAME_LENGTH - extension.length() - 1; // -1 for dot
            String baseName = sanitized.substring(0, maxBaseLength);
            sanitized = baseName + "." + extension;
        }

        return sanitized;
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename File name
     * @return File extension (without dot) or empty string
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    // ==================== Getters for Testing ====================

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public Set<String> getAllowedMimeTypes() {
        return new HashSet<>(allowedMimeTypes);
    }
}