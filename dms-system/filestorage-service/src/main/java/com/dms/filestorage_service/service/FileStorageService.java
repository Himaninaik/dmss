package com.dms.filestorage_service.service;

import com.dms.filestorage_service.model.DocumentMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final RestTemplate restTemplate;
    
    @Value("${metadata.service.url:http://localhost:8081}")
    private String metadataServiceUrl;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.restTemplate = new RestTemplate();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store file with chunk support
     */
    public DocumentMetadata storeFile(MultipartFile file, String title, String description, String uploadedBy) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + fileName);
            }

            // Create unique file name to avoid conflicts
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create metadata object
            DocumentMetadata metadata = new DocumentMetadata();
            metadata.setTitle(title != null ? title : fileName);
            metadata.setDescription(description);
            metadata.setFileName(fileName);
            metadata.setFileSize(file.getSize());
            metadata.setUploadDate(Instant.now());
            metadata.setUploadedBy(uploadedBy != null ? uploadedBy : "anonymous");
            metadata.setStoragePath(uniqueFileName);
            metadata.setContentType(file.getContentType());
            metadata.setVersion(1);

            // Save metadata to metadata service
            DocumentMetadata savedMetadata = saveMetadata(metadata);
            
            return savedMetadata;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Store file chunk (for large file uploads)
     */
    public String storeFileChunk(MultipartFile chunk, String chunkId, int chunkNumber, int totalChunks, String fileName) {
        try {
            System.out.println("FileStorageService: Storing chunk " + chunkNumber + " of " + totalChunks + " for file: " + fileName);
            System.out.println("FileStorageService: Chunk ID: " + chunkId);
            System.out.println("FileStorageService: Chunk size: " + chunk.getSize());
            
            String tempDir = this.fileStorageLocation.toString() + "/temp";
            Path tempDirPath = Paths.get(tempDir);
            Files.createDirectories(tempDirPath);
            
            String chunkFileName = chunkId + "_chunk_" + chunkNumber;
            Path chunkLocation = tempDirPath.resolve(chunkFileName);
            Files.copy(chunk.getInputStream(), chunkLocation, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("FileStorageService: Chunk saved to: " + chunkLocation.toString());

            // If this is the last chunk, merge all chunks
            if (chunkNumber == totalChunks - 1) {
                System.out.println("FileStorageService: Last chunk received, merging files...");
                String mergedFileName = mergeChunks(chunkId, totalChunks, fileName);
                System.out.println("FileStorageService: File merged successfully: " + mergedFileName);
                
                // For file updates, we need to update existing metadata instead of creating new
                // The finalizeUpload method will handle the metadata update
                return mergedFileName; // Return the actual filename instead of a message
            }
            
            return "Chunk " + chunkNumber + " uploaded successfully";
        } catch (IOException ex) {
            System.err.println("FileStorageService: Error storing chunk: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Could not store file chunk. Please try again!", ex);
        }
    }

    /**
     * Merge all chunks into final file
     */
    private String mergeChunks(String chunkId, int totalChunks, String fileName) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        
        System.out.println("FileStorageService: Merging chunks into file: " + uniqueFileName);
        System.out.println("FileStorageService: Target location: " + targetLocation.toString());
        
        try (FileOutputStream fos = new FileOutputStream(targetLocation.toFile())) {
            for (int i = 0; i < totalChunks; i++) {
                String tempDir = this.fileStorageLocation.toString() + "/temp";
                String chunkFileName = chunkId + "_chunk_" + i;
                Path chunkPath = Paths.get(tempDir).resolve(chunkFileName);
                
                System.out.println("FileStorageService: Merging chunk " + i + " from: " + chunkPath.toString());
                
                if (Files.exists(chunkPath)) {
                    Files.copy(chunkPath, fos);
                    Files.delete(chunkPath); // Clean up chunk after merging
                    System.out.println("FileStorageService: Chunk " + i + " merged successfully");
                } else {
                    System.err.println("FileStorageService: Chunk " + i + " not found at: " + chunkPath.toString());
                    throw new IOException("Chunk " + i + " not found at: " + chunkPath.toString());
                }
            }
        }
        
        System.out.println("FileStorageService: File merged successfully: " + uniqueFileName);
        return uniqueFileName;
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    /**
     * Download file in chunks
     */
    public byte[] downloadFileChunk(String fileName, long start, long end) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            File file = filePath.toFile();
            
            if (!file.exists()) {
                throw new RuntimeException("File not found " + fileName);
            }

            long fileSize = file.length();
            long chunkSize = end - start + 1;
            
            if (end >= fileSize) {
                end = fileSize - 1;
                chunkSize = end - start + 1;
            }

            byte[] buffer = new byte[(int) chunkSize];
            
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(start);
                raf.readFully(buffer);
            }
            
            return buffer;
        } catch (IOException ex) {
            System.err.println("FileStorageService: Error reading file chunk: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error reading file chunk", ex);
        }
    }

    /**
     * Delete file
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not determine file size " + fileName, ex);
        }
    }

    /**
     * Save metadata to metadata service
     */
    private DocumentMetadata saveMetadata(DocumentMetadata metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DocumentMetadata> request = new HttpEntity<>(metadata, headers);
            
            ResponseEntity<DocumentMetadata> response = restTemplate.postForEntity(
                metadataServiceUrl + "/api/metadata", 
                request, 
                DocumentMetadata.class
            );
            
            return response.getBody();
        } catch (Exception ex) {
            // If metadata service is unavailable, return the metadata object with id=null
            System.err.println("Warning: Could not save metadata to metadata service: " + ex.getMessage());
            return metadata;
        }
    }

    /**
     * Get metadata from metadata service
     */
    public DocumentMetadata getMetadata(Long id) {
        try {
            ResponseEntity<DocumentMetadata> response = restTemplate.getForEntity(
                metadataServiceUrl + "/api/metadata/" + id, 
                DocumentMetadata.class
            );
            return response.getBody();
        } catch (Exception ex) {
            throw new RuntimeException("Could not retrieve metadata: " + ex.getMessage(), ex);
        }
    }

    /**
     * Finalize upload and update metadata with new file information
     */
    public String finalizeUpload(String chunkId, Long metadataId) {
        try {
            System.out.println("FileStorageService: Finalizing upload for chunkId: " + chunkId + ", metadataId: " + metadataId);
            
            // Find the most recently created file (should be the merged file)
            File uploadDir = this.fileStorageLocation.toFile();
            File[] files = uploadDir.listFiles();
            String newFileName = null;
            
            if (files != null) {
                // Look for the most recently created file
                File latestFile = null;
                long latestTime = 0;
                
                for (File file : files) {
                    if (file.isFile() && file.lastModified() > latestTime) {
                        latestFile = file;
                        latestTime = file.lastModified();
                    }
                }
                
                if (latestFile != null) {
                    newFileName = latestFile.getName();
                    System.out.println("FileStorageService: Found merged file: " + newFileName);
                }
            }
            
            if (newFileName == null) {
                throw new RuntimeException("Could not find merged file for chunkId: " + chunkId);
            }
            
            // Get the current metadata
            DocumentMetadata currentMetadata = getMetadata(metadataId);
            if (currentMetadata == null) {
                throw new RuntimeException("Could not retrieve metadata for ID: " + metadataId);
            }
            
            // Delete the old file if it exists and is different from the new one
            String oldStoragePath = currentMetadata.getStoragePath();
            if (oldStoragePath != null && !oldStoragePath.equals(newFileName)) {
                try {
                    deleteFile(oldStoragePath);
                    System.out.println("FileStorageService: Deleted old file: " + oldStoragePath);
                } catch (Exception ex) {
                    System.err.println("FileStorageService: Warning - Could not delete old file: " + ex.getMessage());
                }
            }
            
            // Update metadata with new file information
            currentMetadata.setStoragePath(newFileName);
            currentMetadata.setFileSize(Files.size(this.fileStorageLocation.resolve(newFileName)));
            currentMetadata.setUploadDate(Instant.now());
            
            // Update the metadata in the metadata service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DocumentMetadata> request = new HttpEntity<>(currentMetadata, headers);
            
            restTemplate.exchange(
                metadataServiceUrl + "/api/metadata/" + metadataId,
                org.springframework.http.HttpMethod.PUT,
                request,
                DocumentMetadata.class
            );
            
            System.out.println("FileStorageService: Metadata updated successfully");
            return "Upload finalized and metadata updated";
            
        } catch (Exception ex) {
            System.err.println("FileStorageService: Error finalizing upload: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Could not finalize upload: " + ex.getMessage(), ex);
        }
    }
}
