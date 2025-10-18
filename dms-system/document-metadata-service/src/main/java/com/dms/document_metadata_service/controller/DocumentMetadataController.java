package com.dms.document_metadata_service.controller;

import com.dms.document_metadata_service.model.DocumentMetadata;
import com.dms.document_metadata_service.service.DocumentMetadataService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metadata")
public class DocumentMetadataController {

    private final DocumentMetadataService service;

    public DocumentMetadataController(DocumentMetadataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentMetadata create(@Valid @RequestBody DocumentMetadata metadata) {
        return service.create(metadata);
    }

    @GetMapping("/{id}")
    public DocumentMetadata get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<DocumentMetadata> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        return service.list(q, page, size, sortBy, direction);
    }

    @PutMapping("/{id}")
    public DocumentMetadata update(@PathVariable Long id, @Valid @RequestBody DocumentMetadata metadata) {
        return service.update(id, metadata);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}




