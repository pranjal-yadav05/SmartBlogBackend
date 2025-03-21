package com.smartblogbackend.repository;

import com.smartblogbackend.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByAuthorEmail(String email);
    Page<BlogPost> findByAuthorEmail(String email, Pageable pageable);
    Page<BlogPost> findAll(Pageable pageable);
    Page<BlogPost> findByCategory(String category, Pageable pageable);
}