package com.smartblogbackend.service;

import com.smartblogbackend.model.User;
import com.smartblogbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<User> getAllUsers(String search) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByNameContainingIgnoreCase(search);
        }
        return userRepository.findAll();
    }
}
