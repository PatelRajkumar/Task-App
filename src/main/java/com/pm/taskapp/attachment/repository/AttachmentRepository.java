package com.pm.taskapp.attachment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pm.taskapp.attachment.entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.isDeleted = false")
    Optional<Attachment> findById(@Param("id") UUID id);

    @EntityGraph(attributePaths = { "issue", "uploadedBy" })
    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.isDeleted = false")
    Optional<Attachment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT a FROM Attachment a " +
            "WHERE a.issue.id = :issueId " +
            "AND a.isDeleted = false " +
            "ORDER BY a.createdAt DESC")
    Page<Attachment> findByIssue(@Param("issueId") UUID issueId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attachment a " +
            "WHERE a.issue.id = :issueId " +
            "AND a.isDeleted = false")
    long countByIssue(@Param("issueId") UUID issueId);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.id = :id AND a.isDeleted = false")
    boolean existsById(@Param("id") UUID id);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.id = :id AND a.isDeleted = true")
    boolean isDeleted(@Param("id") UUID id);

}
