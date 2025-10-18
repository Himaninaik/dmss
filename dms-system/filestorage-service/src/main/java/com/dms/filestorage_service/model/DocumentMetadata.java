package com.dms.filestorage_service.model;

import java.time.Instant;

public class DocumentMetadata {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private Instant uploadDate;
    private String uploadedBy;
    private String storagePath;
    private String contentType;
    private Integer version;

    public DocumentMetadata() {
    }

    public DocumentMetadata(Long id, String title, String description, String fileName, 
                          Long fileSize, Instant uploadDate, String uploadedBy, 
                          String storagePath, String contentType, Integer version) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.version = version;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
