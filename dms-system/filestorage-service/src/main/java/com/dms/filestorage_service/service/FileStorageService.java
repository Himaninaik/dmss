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
            String tempDir = this.fileStorageLocation.toString() + "/temp";
            Path tempDirPath = Paths.get(tempDir);
            Files.createDirectories(tempDirPath);
            
            String chunkFileName = chunkId + "_chunk_" + chunkNumber;
            Path chunkLocation = tempDirPath.resolve(chunkFileName);
            Files.copy(chunk.getInputStream(), chunkLocation, StandardCopyOption.REPLACE_EXISTING);

            // If this is the last chunk, merge all chunks
            if (chunkNumber == totalChunks - 1) {
                return mergeChunks(chunkId, totalChunks, fileName);
            }
            
            return "Chunk " + chunkNumber + " uploaded successfully";
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file chunk. Please try again!", ex);
        }
    }

    /**
     * Merge all chunks into final file
     */
    private String mergeChunks(String chunkId, int totalChunks, String fileName) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        
        try (FileOutputStream fos = new FileOutputStream(targetLocation.toFile())) {
            for (int i = 0; i < totalChunks; i++) {
                String tempDir = this.fileStorageLocation.toString() + "/temp";
                String chunkFileName = chunkId + "_chunk_" + i;
                Path chunkPath = Paths.get(tempDir).resolve(chunkFileName);
                
                Files.copy(chunkPath, fos);
                Files.delete(chunkPath); // Clean up chunk after merging
            }
        }
        
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
}
