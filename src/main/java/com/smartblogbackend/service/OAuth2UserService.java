package com.smartblogbackend.service;

import com.smartblogbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Process user details from Google
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");
        
        // Check if user exists in the database
        Optional<User> existingUser = userService.findUserByEmail(email);
        
        if (existingUser.isPresent()) {
            // Update user details if needed
            User user = existingUser.get();
            // Only update profile image if it doesn't exist
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage(pictureUrl);
                userService.saveUser(user);
            }
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setProfileImage(pictureUrl);
            // Generate a random password since we don't need it for OAuth users
            newUser.setPassword(UUID.randomUUID().toString());
            userService.saveUser(newUser);
        }
        
        return oAuth2User;
    }
} 