package com.smartblogbackend.controller;

import com.smartblogbackend.model.BlogPost;
import com.smartblogbackend.model.User;
import com.smartblogbackend.service.BlogPostService;
import com.smartblogbackend.service.CloudinaryService;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.service.GeminiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

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
    private GeminiAIService geminiAIService;  // Inject AI Service

    @Autowired
    private CloudinaryService cloudinaryService;

    public BlogPostController() {}

    @GetMapping("/")
    public List<BlogPost> getAllPosts() {
        return blogPostService.getAllPosts();
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
                imageUrl = cloudinaryService.uploadImage(imageFile.getBytes()); // ✅ Upload image to Cloudinary
            }

            BlogPost blogPost = new BlogPost();
            blogPost.setTitle(title);
            blogPost.setContent(content);
            blogPost.setCategory(category);
            blogPost.setAuthor(user);
            blogPost.setImageUrl(imageUrl); // ✅ Store Cloudinary image URL

            BlogPost savedPost = blogPostService.createPost(blogPost);
            return ResponseEntity.ok(savedPost);
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

            // Call Gemini AI API to generate content
            String generatedContent = geminiAIService.generateBlogPost(prompt);

            // ✅ Only return the generated content, DO NOT save to the database
            Map<String, String> response = Map.of(
                    "title", "AI-Generated: " + prompt,
                    "content", generatedContent
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate post: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestHeader("email") String userEmail) {
        try {
            BlogPost post = blogPostService.getPostById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            if (!post.getAuthor().getEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own posts.");
            }

            blogPostService.deletePost(id);
            return ResponseEntity.ok("Post deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete post: " + e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<?> getPostsByUser(@PathVariable String email) {
        List<BlogPost> posts = blogPostService.getPostsByUser(email);
        if (posts.isEmpty()) {
            return ResponseEntity.status(404).body("No posts found for this user");
        }
        return ResponseEntity.ok(posts); // ✅ Return user's posts
    }

}
