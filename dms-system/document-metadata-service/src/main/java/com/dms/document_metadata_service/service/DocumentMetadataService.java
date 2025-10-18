package com.dms.document_metadata_service.service;

import com.dms.document_metadata_service.model.DocumentMetadata;
import com.dms.document_metadata_service.repository.DocumentMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class DocumentMetadataService {

    private final DocumentMetadataRepository repository;

    public DocumentMetadataService(DocumentMetadataRepository repository) {
        this.repository = repository;
    }

    public DocumentMetadata create(DocumentMetadata metadata) {
        metadata.setUploadDate(Instant.now());
        return repository.save(metadata);
    }

    public DocumentMetadata get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    public Page<DocumentMetadata> list(String query, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by("DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        if (query == null || query.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.search(query, pageable);
    }

    public DocumentMetadata update(Long id, DocumentMetadata input) {
        DocumentMetadata existing = get(id);
        existing.setTitle(input.getTitle());
        existing.setDescription(input.getDescription());
        existing.setFileName(input.getFileName());
        existing.setFileSize(input.getFileSize());
        existing.setUploadedBy(input.getUploadedBy());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}




