package com.dms.document_metadata_service.repository;

import com.dms.document_metadata_service.model.DocumentMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

    @Query("SELECT d FROM DocumentMetadata d WHERE " +
            "(:q IS NULL OR lower(d.title) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(d.description) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(d.fileName) LIKE lower(concat('%', :q, '%')) " +
            "OR lower(d.uploadedBy) LIKE lower(concat('%', :q, '%')))")
    Page<DocumentMetadata> search(@Param("q") String query, Pageable pageable);
}




