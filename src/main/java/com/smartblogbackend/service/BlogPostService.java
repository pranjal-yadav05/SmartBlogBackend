package com.smartblogbackend.service;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.repository.BlogPostRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogPostService {
    @Autowired
    private BlogPostRepository blogPostRepository;

    public BlogPostService() {
    }

    public List<BlogPost> getAllPosts() {
        return this.blogPostRepository.findAll();
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
}
