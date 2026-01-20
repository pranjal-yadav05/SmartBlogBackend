package com.smartblogbackend.controller;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.model.DraftPost;
import com.smartblogbackend.model.User;
import com.smartblogbackend.service.BlogPostService;
import com.smartblogbackend.service.CloudinaryService;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.service.GeminiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private CloudinaryService cloudinaryService;

    public BlogPostController() {}

    // --- Published Post Endpoints ---

    @GetMapping("/")
    public List<BlogPost> getAllPosts() {
        return blogPostService.getAllPosts();
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllPostsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BlogPost> pageResult = blogPostService.getAllPosts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/categories/counts")
    public ResponseEntity<List<Map<String, Object>>> getCategoryCounts() {
        return ResponseEntity.ok(blogPostService.getCategoriesWithCounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        Optional<BlogPost> blogPost = blogPostService.getPostById(id);
        return blogPost.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam("authorEmail") String authorEmail,
            @RequestParam(value = "published", defaultValue = "true") boolean published,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            if (title.isEmpty() || content.isEmpty() || authorEmail.isEmpty()) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            User user = userService.findUserByEmail(authorEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadImage(imageFile.getBytes());
            }

            if (published) {
                BlogPost blogPost = new BlogPost();
                blogPost.setTitle(title);
                blogPost.setContent(content);
                blogPost.setCategory(category);
                blogPost.setAuthor(user);
                blogPost.setImageUrl(imageUrl);
                BlogPost savedPost = blogPostService.createPost(blogPost);
                return ResponseEntity.ok(savedPost);
            } else {
                DraftPost draftPost = new DraftPost();
                draftPost.setTitle(title);
                draftPost.setContent(content);
                draftPost.setCategory(category);
                draftPost.setAuthor(user);
                draftPost.setImageUrl(imageUrl);
                DraftPost savedDraft = blogPostService.createDraft(draftPost);
                return ResponseEntity.ok(savedDraft);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create post: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePost(@RequestBody Map<String, Object> payload) {
        try {
            String prompt = (String) payload.get("prompt");
            String authorEmail = (String) payload.get("authorEmail");

            if (prompt == null || prompt.isEmpty() || authorEmail == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            String generatedContent = geminiAIService.generateBlogPost(prompt);

            Map<String, String> response = Map.of(
                    "title", "AI-Generated: " + prompt,
                    "content", generatedContent
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate post: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePostWithImage(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile imageFile,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        BlogPost post = blogPostService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only update your own posts.");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(imageFile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
            }
        }

        BlogPost updated = blogPostService.updatePost(id, title, content, category, imageUrl);
        return ResponseEntity.ok(updated);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody BlogPost updatedPost,
            Authentication authentication) {

        String userEmail = authentication.getName();
        BlogPost post = blogPostService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own posts.");
        }

        BlogPost saved = blogPostService.updatePost(
                id,
                updatedPost.getTitle(),
                updatedPost.getContent(),
                updatedPost.getCategory(),
                null
        );

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        BlogPost post = blogPostService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own posts.");
        }

        blogPostService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully.");
    }

    @GetMapping("/user/{email}/paginated")
    public ResponseEntity<Map<String, Object>> getPostsByUserPaginated(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BlogPost> pageResult = blogPostService.getPostsByUser(email, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // --- Draft Endpoints ---

    @GetMapping("/drafts/user/{email}")
    public ResponseEntity<List<DraftPost>> getDraftsByUser(@PathVariable String email) {
        return ResponseEntity.ok(blogPostService.getDraftsByUser(email));
    }

    @PostMapping("/drafts/{id}/publish")
    public ResponseEntity<?> publishDraft(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        DraftPost draft = blogPostService.getDraftById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        if (!draft.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only publish your own drafts.");
        }

        return ResponseEntity.ok(blogPostService.publishDraft(id));
    }

    @DeleteMapping("/drafts/{id}")
    public ResponseEntity<?> deleteDraft(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        DraftPost draft = blogPostService.getDraftById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        if (!draft.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own drafts.");
        }

        blogPostService.deleteDraft(id);
        return ResponseEntity.ok("Draft deleted successfully.");
    }

    @PutMapping(value = "/drafts/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDraftWithImage(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile imageFile,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        DraftPost draft = blogPostService.getDraftById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        if (!draft.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own drafts.");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(imageFile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
            }
        }

        DraftPost updated = blogPostService.updateDraft(id, title, content, category, imageUrl);
        return ResponseEntity.ok(updated);
    }

    @PutMapping(value = "/drafts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDraft(
            @PathVariable Long id,
            @RequestBody DraftPost updatedDraft,
            Authentication authentication) {

        String userEmail = authentication.getName();
        DraftPost draft = blogPostService.getDraftById(id)
                .orElseThrow(() -> new RuntimeException("Draft not found"));

        if (!draft.getAuthor().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own drafts.");
        }

        DraftPost saved = blogPostService.updateDraft(
                id,
                updatedDraft.getTitle(),
                updatedDraft.getContent(),
                updatedDraft.getCategory(),
                null
        );

        return ResponseEntity.ok(saved);
    }
}
