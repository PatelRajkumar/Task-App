package com.pm.taskapp.comments.repository;

import com.pm.taskapp.comments.entity.Comment;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT i FROM Comment i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Comment> findById(@Param("id") UUID id);

    @EntityGraph(attributePaths = { "issue", "author" })
    @Query("SELECT i FROM Comment i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Comment> findByIdWithDetails(@Param("id") UUID id);

    boolean existsById(UUID id);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.issue.id = :issueId " +
            "AND c.isDeleted = false " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findByIssue(@Param("issueId") UUID issueId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c " +
       "WHERE c.issue.id = :issueId " +
       "AND c.isDeleted = false")
    long countByIssue(@Param("issueId") UUID issueId);

    @EntityGraph(attributePaths = { "author" })
    @Query("SELECT c FROM Comment c " +
            "WHERE c.issue.id = :issueId " +
            "AND c.isDeleted = false " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findByIssueWithAuthor(@Param("issueId") UUID issueId, Pageable pageable);

    @EntityGraph(attributePaths = { "issue" })
    @Query("SELECT c FROM Comment c " +
            "WHERE c.author.id = :authorId " +
            "AND c.isDeleted = false " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.author.id = :authorId " +
            "AND c.isDeleted = false")
    long countByAuthor(@Param("authorId") UUID authorId);

    @EntityGraph(attributePaths = { "issue", "author" })
    @Query("SELECT c FROM Comment c " +
            "WHERE c.issue.project.id = :projectId " +
            "AND c.isDeleted = false " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findByProject(@Param("projectId") UUID projectId, Pageable pageable);

    @Query("SELECT c.isDeleted FROM Comment c WHERE c.id = :id")
    Optional<Boolean> isDeleted(@Param("id") UUID id);
}
