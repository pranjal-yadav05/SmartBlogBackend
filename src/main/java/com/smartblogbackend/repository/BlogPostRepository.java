package com.smartblogbackend.repository;

import com.smartblogbackend.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<BlogPost> findByAuthorEmail(String email);

    @EntityGraph(attributePaths = {"author"})
    Page<BlogPost> findByAuthorEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<BlogPost> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<BlogPost> findByCategory(String category, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author"})
    Optional<BlogPost> findById(Long id);

    @org.springframework.data.jpa.repository.Query("SELECT p.category as name, COUNT(p) as count FROM BlogPost p GROUP BY p.category")
    List<Map<String, Object>> countPostsByCategory();
}