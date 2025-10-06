package com.thinkable.backend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling file uploads and management with Google Cloud Storage
 * Supports all file types: PDFs, videos, images, documents, audio files
 */
@Service
public class GoogleCloudStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageService.class);
    
    private final Storage storage;
    private final String bucketName;
    
    // Supported file types for educational content
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        // Documents
        "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
        "txt", "rtf", "odt", "odp", "ods",
        // Images
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp",
        // Videos
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v",
        // Audio
        "mp3", "wav", "aac", "ogg", "m4a", "flac",
        // Interactive Content
        "h5p"
    );
    
    // Max file size: 500MB (in bytes) - generous for video content
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;
    
    public GoogleCloudStorageService(@Value("${gcs.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        
        try {
            // Try to load credentials from environment variable first (for production)
            String credentialsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
            
            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                logger.info("Loading GCS credentials from environment variable");
                java.io.ByteArrayInputStream credentialsStream = new java.io.ByteArrayInputStream(credentialsJson.getBytes());
                this.storage = StorageOptions.newBuilder()
                        .setCredentials(com.google.auth.oauth2.ServiceAccountCredentials.fromStream(credentialsStream))
                        .build()
                        .getService();
            } else {
                // Fallback to JSON file (for local development)
                String credentialsPath = "../gcs-service-account.json";
                java.nio.file.Path path = java.nio.file.Paths.get(credentialsPath);
                
                if (java.nio.file.Files.exists(path)) {
                    logger.info("Loading GCS credentials from file: {}", credentialsPath);
                    this.storage = StorageOptions.newBuilder()
                            .setCredentials(com.google.auth.oauth2.ServiceAccountCredentials
                                    .fromStream(java.nio.file.Files.newInputStream(path)))
                            .build()
                            .getService();
                } else {
                    logger.warn("No GCS credentials found (file or env var), falling back to default credentials");
                    this.storage = StorageOptions.getDefaultInstance().getService();
                }
            }
            
            logger.info("GoogleCloudStorageService initialized with bucket: {}", bucketName);
            
        } catch (Exception e) {
            logger.error("Failed to initialize GoogleCloudStorageService: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Google Cloud Storage", e);
        }
    }
    
    /**
     * Upload a file to Google Cloud Storage
     * @param file The multipart file to upload
     * @param folder Optional folder name for organization (e.g., "learning-content", "profile-images")
     * @return GCSUploadResult containing the upload details
     * @throws IOException if upload fails
     */
    public GCSUploadResult uploadFile(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        
        try {
            // Generate unique filename while preserving extension
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create object name with folder structure
            String objectName = folder != null ? folder + "/" + uniqueFilename : uniqueFilename;
            
            logger.info("Uploading file: {} ({}bytes) to GCS bucket: {} as: {}", 
                       originalFilename, file.getSize(), bucketName, objectName);
            
            // Create blob info with proper content type
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                    .setContentType(file.getContentType())
                    .build();
            
            // Upload the file
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            GCSUploadResult result = new GCSUploadResult(
                blob.getName(),
                blob.getMediaLink(),
                blob.getContentType(),
                blob.getSize(),
                originalFilename,
                bucketName
            );
            
            logger.info("File uploaded successfully. Object name: {}, Size: {} bytes", 
                       blob.getName(), blob.getSize());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new IOException("Failed to upload file to Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Upload H5P extracted file to Google Cloud Storage
     * Specialized method for H5P content files with proper content types
     * @param fileBytes The file content as byte array
     * @param objectName The GCS object name (with path)
     * @param contentType The MIME type of the file
     * @return The signed URL for accessing the uploaded file
     */
    public String uploadH5PFile(byte[] fileBytes, String objectName, String contentType) throws IOException {
        try {
            logger.info("Uploading H5P file: {} ({}bytes) to GCS bucket: {} with content type: {}", 
                       objectName, fileBytes.length, bucketName, contentType);
            
            // Create blob info with proper content type
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
                .setContentType(contentType)
                .build();
            
            // Upload the file
            Blob blob = storage.create(blobInfo, fileBytes);
            
            // Generate signed URL valid for 24 hours (longer for H5P content)
            URL signedUrl = storage.signUrl(
                blobInfo, 
                24 * 60, // 24 hours
                TimeUnit.MINUTES
            );
            
            logger.info("H5P file uploaded successfully. Object name: {}, Size: {} bytes", 
                       objectName, fileBytes.length);
            
            return signedUrl.toString();
            
        } catch (Exception e) {
            logger.error("Failed to upload H5P file {}: {}", objectName, e.getMessage());
            throw new IOException("Failed to upload H5P file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a signed URL for secure file access
     * @param objectName The name of the object in GCS
     * @param durationMinutes How long the URL should be valid (default: 60 minutes)
     * @return Signed URL string
     */
    public String generateSignedUrl(String objectName, int durationMinutes) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
            
            // Generate signed URL valid for specified duration
            URL signedUrl = storage.signUrl(
                blobInfo, 
                durationMinutes, 
                TimeUnit.MINUTES
            );
            
            logger.debug("Generated signed URL for object: {} (valid for {} minutes)", 
                        objectName, durationMinutes);
            
            return signedUrl.toString();
            
        } catch (Exception e) {
            logger.error("Failed to generate signed URL for object {}: {}", objectName, e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate a signed URL with default 1-hour expiration
     */
    public String generateSignedUrl(String objectName) {
        return generateSignedUrl(objectName, 60);
    }
    
    /**
     * Delete a file from Google Cloud Storage
     * @param objectName The name of the object to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(String objectName) {
        try {
            boolean deleted = storage.delete(bucketName, objectName);
            
            if (deleted) {
                logger.info("File deleted successfully: {}", objectName);
            } else {
                logger.warn("File not found or already deleted: {}", objectName);
            }
            
            return deleted;
            
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", objectName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a file exists in Google Cloud Storage
     * @param objectName The name of the object to check
     * @return true if file exists
     */
    public boolean fileExists(String objectName) {
        try {
            Blob blob = storage.get(bucketName, objectName);
            return blob != null && blob.exists();
        } catch (Exception e) {
            logger.error("Error checking if file exists {}: {}", objectName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("Invalid filename");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_FILE_TYPES.contains(extension)) {
            throw new IOException("File type '" + extension + "' is not allowed. Allowed types: " + ALLOWED_FILE_TYPES);
        }
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    /**
     * Result object for Google Cloud Storage uploads
     */
    public static class GCSUploadResult {
        private final String objectName;
        private final String mediaLink;
        private final String contentType;
        private final long size;
        private final String originalFilename;
        private final String bucketName;
        
        public GCSUploadResult(String objectName, String mediaLink, String contentType, 
                             long size, String originalFilename, String bucketName) {
            this.objectName = objectName;
            this.mediaLink = mediaLink;
            this.contentType = contentType;
            this.size = size;
            this.originalFilename = originalFilename;
            this.bucketName = bucketName;
        }
        
        // Getters
        public String getObjectName() { return objectName; }
        public String getMediaLink() { return mediaLink; }
        public String getContentType() { return contentType; }
        public long getSize() { return size; }
        public String getOriginalFilename() { return originalFilename; }
        public String getBucketName() { return bucketName; }
    }
}
