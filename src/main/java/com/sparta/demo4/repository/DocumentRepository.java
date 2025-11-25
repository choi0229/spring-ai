package com.sparta.demo4.repository;

import com.sparta.demo4.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByFilenameContaining(String filename);

    List<DocumentEntity> findByContentType(String contentType);
}
