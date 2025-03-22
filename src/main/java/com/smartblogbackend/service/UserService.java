package com.smartblogbackend.service;

import com.smartblogbackend.model.User;
import com.smartblogbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public UserService() {
    }

    public Optional<User> findUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        User savedUser = userRepository.save(user);

        // Send welcome email after user is successfully saved
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

        return savedUser;
    }

    // Legacy method - returns all users without pagination (not recommended for large datasets)
    public List<User> getAllUsers(String search) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByNameContainingIgnoreCase(search);
        }
        return userRepository.findAll();
    }
    
    // New method with pagination and enhanced search
    public Page<User> searchUsers(String query, int page, int size, String sortBy, String direction) {
        // Default sort by name if not specified
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "name";
        }
        
        // Create sort direction
        Sort sort = direction != null && direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Perform search with pagination
        Page<User> userPage = userRepository.searchUsers(query, pageable);
        
        // Remove passwords from results
        userPage.getContent().forEach(user -> user.setPassword(null));
        
        return userPage;
    }
    
    // Method to search users by first letter of name (for alphabetical search)
    public Page<User> findUsersByInitial(String initial, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<User> userPage = userRepository.findByNameStartingWithIgnoreCase(initial, pageable);
        
        // Remove passwords from results
        userPage.getContent().forEach(user -> user.setPassword(null));
        
        return userPage;
    }
}
