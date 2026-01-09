package com.smartblogbackend.controller;

import com.smartblogbackend.model.AuthenticationResponse;
import com.smartblogbackend.model.User;
import com.smartblogbackend.service.CloudinaryService;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    private JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return this.userService.saveUser(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("======== inside getUserProfile --=========");
            // Extract token from Authorization header (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            // Extract user email from JWT token
            String email = jwtUtil.extractEmail(token);
            System.out.println("email " + email);
            Optional<User> user = userService.findUserByEmail(email);
            if (user.isPresent()) {
                User foundUser = user.get();
                // Don't send password back to client
                foundUser.setPassword(null);
                return ResponseEntity.ok(foundUser);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired token"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // Require a search term to avoid returning all users
        if (search == null || search.trim().isEmpty() || search.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // If search is provided, use the regular search flow
        List<User> users = userService.getAllUsers(search);
        // Remove password from response
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        
        // Return empty results if query is empty or too short
        if (query == null || query.trim().isEmpty() || query.trim().length() < 2) {
            return ResponseEntity.ok(Map.of(
                "content", List.of(),
                "totalElements", 0,
                "totalPages", 0,
                "size", size,
                "number", page,
                "empty", true
            ));
        }
        
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 10; // Default size if invalid
        }
        
        Page<User> userPage = userService.searchUsers(query, page, size, sortBy, direction);
        return ResponseEntity.ok(userPage);
    }
    
    @GetMapping("/by-initial/{initial}")
    public ResponseEntity<?> findUsersByInitial(
            @PathVariable String initial,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        if (initial == null || initial.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Initial letter is required"));
        }
        
        Page<User> userPage = userService.findUsersByInitial(initial, page, size);
        return ResponseEntity.ok(userPage);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        Optional<User> existingUser = userService.findUserByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();
            if (foundUser.getPassword().equals(user.getPassword())) {
                // Generate JWT token using the full User object
                String token = jwtUtil.generateJwtToken(foundUser);
                return ResponseEntity.ok(new AuthenticationResponse(token));
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid password"));
            }
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "password", required = false) String newPassword,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        try {
            // Extract token from Authorization header (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            // Extract user email from JWT token
            String tokenEmail = jwtUtil.extractEmail(token);

            // Verify the email in token matches the email being updated
            if (!tokenEmail.equals(email)) {
                return ResponseEntity.status(403).body(Map.of("message", "You can only update your own profile"));
            }

            Optional<User> existingUserOpt = userService.findUserByEmail(email);
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "User not found"));
            }

            User user = existingUserOpt.get();

            // If changing password, validate appropriately
            if (newPassword != null && !newPassword.isEmpty()) {
                String existingPassword = user.getPassword();

                // Heuristic: if the existing password looks like a generated UUID,
                // treat this account as Google-OAuth-only and allow setting a
                // real password for the first time without requiring currentPassword.
                boolean looksLikeOauthGenerated =
                        existingPassword != null
                                && existingPassword.length() == 36
                                && existingPassword.chars().filter(ch -> ch == '-').count() == 4;

                if (!looksLikeOauthGenerated) {
                    // Regular local account: require current password
                    if (currentPassword == null || !existingPassword.equals(currentPassword)) {
                        return ResponseEntity.status(400).body(Map.of("message", "Current password is incorrect"));
                    }
                }

                // Update password (both for regular and OAuth-first-time-password cases)
                user.setPassword(newPassword);
            }

            // Update name
            user.setName(name);

            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                try {
                    byte[] imageBytes = image.getBytes();
                    String imageUrl = cloudinaryService.uploadImage(imageBytes);
                    user.setProfileImage(imageUrl);
                } catch (IOException e) {
                    return ResponseEntity.status(500).body(Map.of("message", "Failed to upload image"));
                }
            }

            // Save updated user
            User updatedUser = userService.saveUser(user);

            // Don't return password in response
            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error updating profile: " + e.getMessage()));
        }
    }
}