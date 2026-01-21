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

    @Autowired
    private com.smartblogbackend.repository.UserRepository userRepository;

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

    public Page<java.util.Map<String, Object>> getCategoriesWithCounts(Pageable pageable) {
        return blogPostRepository.countPostsByCategory(pageable);
    }

    public Page<java.util.Map<String, Object>> searchCategoriesWithCounts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return blogPostRepository.countPostsByCategory(pageable);
        }
        return blogPostRepository.searchCategoriesWithCounts(query, pageable);
    }

    @jakarta.transaction.Transactional
    public void incrementViews(Long id) {
        blogPostRepository.incrementViews(id);
    }

    // --- Engagement Methods (Claps & Comments) ---

    @Autowired
    private com.smartblogbackend.repository.CommentRepository commentRepository;

    @jakarta.transaction.Transactional
    public void incrementClaps(Long id, int amount) {
        // limit standard clap amount per request to prevent abuse if needed, 
        // but user requested creating a feature where user can clap multiple times.
        // We will trust the controller to pass reasonable amounts (e.g. 1).
        blogPostRepository.incrementClaps(id, amount);
    }

    public com.smartblogbackend.model.Comment addComment(Long postId, String content, String userEmail) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        com.smartblogbackend.model.User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.smartblogbackend.model.Comment comment = new com.smartblogbackend.model.Comment(content, post, author);
        return commentRepository.save(comment);
    }

    public List<com.smartblogbackend.model.Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }
}
