package com.smartblogbackend.service;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.model.DraftPost;
import com.smartblogbackend.repository.BlogPostRepository;
import com.smartblogbackend.repository.DraftPostRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogPostService {
    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private DraftPostRepository draftPostRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public BlogPostService() {
    }

    // --- Published Post Methods ---

    public List<BlogPost> getAllPosts() {
        return this.blogPostRepository.findAll();
    }
    
    public Page<BlogPost> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    public BlogPost createPost(BlogPost post) {
        return this.blogPostRepository.save(post);
    }

    public Optional<BlogPost> getPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    public void deletePost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + id));
        
        // Delete image from Cloudinary if it exists
        if (post.getImageUrl() != null) {
            cloudinaryService.deleteImage(post.getImageUrl());
        }
        
        blogPostRepository.delete(post);
    }

    public BlogPost updatePost(Long id, String title, String content, String category, String imageUrl) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + id));

        existingPost.setTitle(title);
        existingPost.setContent(content);
        existingPost.setCategory(category);

        if (imageUrl != null && !imageUrl.isBlank()) {
            // Delete old image if it exists
            if (existingPost.getImageUrl() != null) {
                cloudinaryService.deleteImage(existingPost.getImageUrl());
            }
            existingPost.setImageUrl(imageUrl);
        }

        return blogPostRepository.save(existingPost);
    }

    public List<BlogPost> getPostsByUser(String email) {
        return blogPostRepository.findByAuthorEmail(email);
    }
    
    public Page<BlogPost> getPostsByUser(String email, Pageable pageable) {
        return blogPostRepository.findByAuthorEmail(email, pageable);
    }
    
    public Page<BlogPost> getPostsByCategory(String category, Pageable pageable) {
        return blogPostRepository.findByCategory(category, pageable);
    }

    // --- Draft Post Methods ---

    public DraftPost createDraft(DraftPost draft) {
        return draftPostRepository.save(draft);
    }

    public Optional<DraftPost> getDraftById(Long id) {
        return draftPostRepository.findById(id);
    }

    public List<DraftPost> getDraftsByUser(String email) {
        return draftPostRepository.findByAuthorEmail(email);
    }
    
    public Page<DraftPost> getDraftsByUser(String email, Pageable pageable) {
        return draftPostRepository.findByAuthorEmail(email, pageable);
    }

    public void deleteDraft(Long id) {
        DraftPost draft = draftPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found with ID: " + id));
        
        // Delete image from Cloudinary if it exists
        if (draft.getImageUrl() != null) {
            cloudinaryService.deleteImage(draft.getImageUrl());
        }
        
        draftPostRepository.delete(draft);
    }

    public DraftPost updateDraft(Long id, String title, String content, String category, String imageUrl) {
        DraftPost existingDraft = draftPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found with ID: " + id));

        existingDraft.setTitle(title);
        existingDraft.setContent(content);
        existingDraft.setCategory(category);

        if (imageUrl != null && !imageUrl.isBlank()) {
            // Delete old image if it exists
            if (existingDraft.getImageUrl() != null) {
                cloudinaryService.deleteImage(existingDraft.getImageUrl());
            }
            existingDraft.setImageUrl(imageUrl);
        }

        return draftPostRepository.save(existingDraft);
    }

    @Transactional
    public BlogPost publishDraft(Long draftId) {
        DraftPost draft = draftPostRepository.findById(draftId)
                .orElseThrow(() -> new RuntimeException("Draft not found with ID: " + draftId));

        BlogPost post = new BlogPost();
        post.setTitle(draft.getTitle());
        post.setContent(draft.getContent());
        post.setCategory(draft.getCategory());
        post.setAuthor(draft.getAuthor());
        post.setImageUrl(draft.getImageUrl());
        post.setCreatedAt(draft.getCreatedAt());

        BlogPost savedPost = blogPostRepository.save(post);
        draftPostRepository.delete(draft);
        
        return savedPost;
    }

    public List<java.util.Map<String, Object>> getCategoriesWithCounts() {
        return blogPostRepository.countPostsByCategory();
    }
}
