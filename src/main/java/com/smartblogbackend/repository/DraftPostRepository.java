package com.smartblogbackend.repository;

import com.smartblogbackend.model.DraftPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DraftPostRepository extends JpaRepository<DraftPost, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<DraftPost> findByAuthorEmail(String email);

    @EntityGraph(attributePaths = {"author"})
    Page<DraftPost> findByAuthorEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<DraftPost> findByCategory(String category, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Optional<DraftPost> findById(Long id);
}
