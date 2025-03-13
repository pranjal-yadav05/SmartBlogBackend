package com.smartblogbackend.controller;

import com.smartblogbackend.model.User;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private JwtUtil jwtUtil = new JwtUtil(); // Initialize JwtUtil

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return this.userService.saveUser(user);
    }

    @GetMapping("/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return this.userService.findUserByEmail(email);
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
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public static class AuthenticationResponse {
        private String token;

        public AuthenticationResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
