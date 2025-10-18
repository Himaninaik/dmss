package com.dms.filestorage_service.controller;

import com.dms.filestorage_service.model.DocumentMetadata;
import com.dms.filestorage_service.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload a file
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentMetadata> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        
        DocumentMetadata metadata = fileStorageService.storeFile(file, title, description, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(metadata);
    }

    /**
     * Upload file chunk (for large files)
     */
    @PostMapping("/upload-chunk")
    public ResponseEntity<Map<String, String>> uploadFileChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("chunkId") String chunkId,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileName") String fileName) {
        
        String result = fileStorageService.storeFileChunk(chunk, chunkId, chunkNumber, totalChunks, fileName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        response.put("chunkNumber", String.valueOf(chunkNumber));
        response.put("totalChunks", String.valueOf(totalChunks));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletRequest request) {
        
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Download file chunk (for large files)
     */
    @GetMapping("/download-chunk/{fileName:.+}")
    public ResponseEntity<byte[]> downloadFileChunk(
            @PathVariable String fileName,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        
        long fileSize = fileStorageService.getFileSize(fileName);
        
        long start = 0;
        long end = fileSize - 1;
        
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        }
        
        byte[] data = fileStorageService.downloadFileChunk(fileName, start, end);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
        headers.setContentLength(data.length);
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        
        return new ResponseEntity<>(data, headers, 
                rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        response.put("fileName", fileName);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get file metadata
     */
    @GetMapping("/metadata/{id}")
    public ResponseEntity<DocumentMetadata> getFileMetadata(@PathVariable Long id) {
        DocumentMetadata metadata = fileStorageService.getMetadata(id);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get file info
     */
    @GetMapping("/info/{fileName:.+}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileName) {
        long fileSize = fileStorageService.getFileSize(fileName);
        
        Map<String, Object> info = new HashMap<>();
        info.put("fileName", fileName);
        info.put("fileSize", fileSize);
        info.put("exists", true);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "filestorage-service");
        
        return ResponseEntity.ok(response);
    }
}
