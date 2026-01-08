package com.smartblogbackend.service;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.repository.BlogPostRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@Service
public class BlogPostService {
    @Autowired
    private BlogPostRepository blogPostRepository;

    public BlogPostService() {
    }

    public List<BlogPost> getAllPosts() {
        return this.blogPostRepository.findAll();
    }
    
    public Page<BlogPost> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    public BlogPost createPost(BlogPost post) {
        return (BlogPost)this.blogPostRepository.save(post);
    }

    public Optional<BlogPost> getPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    public void deletePost(Long id) {
        if (blogPostRepository.existsById(id)) {
            blogPostRepository.deleteById(id);
        } else {
            throw new RuntimeException("Post not found with ID: " + id);
        }
    }

    public BlogPost updatePost(
            Long id,
            String title,
            String content,
            String category,
            String imageUrl
    ) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + id));

        existingPost.setTitle(title);
        existingPost.setContent(content);
        existingPost.setCategory(category);

        // Only update image if a new one was uploaded
        if (imageUrl != null && !imageUrl.isBlank()) {
            existingPost.setImageUrl(imageUrl);
        }

        return blogPostRepository.save(existingPost);
    }

    
    public List<BlogPost> getPostsByUser(String email) {
        return blogPostRepository.findByAuthorEmail(email); // ✅ Fetch user's posts
    }
    
    public Page<BlogPost> getPostsByUser(String email, Pageable pageable) {
        return blogPostRepository.findByAuthorEmail(email, pageable);
    }
    
    public Page<BlogPost> getPostsByCategory(String category, Pageable pageable) {
        return blogPostRepository.findByCategory(category, pageable);
    }
}
