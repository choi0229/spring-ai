package com.example.loyalty.repository;

import com.example.loyalty.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByFilenameContaining(String filename);

    List<DocumentEntity> findByContentType(String contentType);
}
