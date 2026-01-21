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

    @org.springframework.data.jpa.repository.Query(value = "SELECT p.category as name, COUNT(p) as count FROM BlogPost p GROUP BY p.category", 
            countQuery = "SELECT COUNT(DISTINCT p.category) FROM BlogPost p")
    Page<Map<String, Object>> countPostsByCategory(Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT p.category as name, COUNT(p) as count FROM BlogPost p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%')) GROUP BY p.category",
            countQuery = "SELECT COUNT(DISTINCT p.category) FROM BlogPost p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Map<String, Object>> searchCategoriesWithCounts(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE BlogPost p SET p.views = p.views + 1 WHERE p.id = :id")
    void incrementViews(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE BlogPost p SET p.claps = p.claps + :amount WHERE p.id = :id")
    void incrementClaps(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("amount") int amount);
}