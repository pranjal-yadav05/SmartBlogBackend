package com.smartblogbackend.controller;

import com.smartblogbackend.model.AuthenticationResponse;
import com.smartblogbackend.model.User;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/oauth2")
public class OAuthController {

    private static final Logger logger = Logger.getLogger(OAuthController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/login/oauth2/code/google")
    public RedirectView handleGoogleOAuthCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        // Delegate to the existing success handler
        return oauthSuccess(oauth2User);
    }

    @GetMapping("/success")
    public RedirectView oauthSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        logger.info("OAuth success endpoint called");
        
        // Check if OAuth2User is null
        if (oauth2User == null) {
            logger.severe("OAuth2User is null - authentication failed");
            return new RedirectView(frontendUrl + "/login?error=auth_failed");
        }
        
        // Get user details from OAuth2User
        String email = oauth2User.getAttribute("email");
        logger.info("OAuth authentication for email: " + email);
        
        if (email == null) {
            logger.severe("Email not found in OAuth attributes");
            return new RedirectView(frontendUrl + "/login?error=email_missing");
        }
        
        // Find user in the database
        Optional<User> userOptional = userService.findUserByEmail(email);
        
        if (!userOptional.isPresent()) {
            logger.info("User not found in database, creating new user");
            // Create new user from OAuth data
            try {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(oauth2User.getAttribute("name"));
                newUser.setProfileImage(oauth2User.getAttribute("picture"));
                // Set a random password since we don't need it for OAuth users
                newUser.setPassword(java.util.UUID.randomUUID().toString());
                
                userService.saveUser(newUser);
                
                // Generate JWT token for the new user
                String token = jwtUtil.generateJwtToken(newUser);
                logger.info("New user created and JWT token generated");
                
                // Redirect to frontend with token
                return new RedirectView(frontendUrl + "/oauth/callback?token=" + token);
            } catch (Exception e) {
                logger.severe("Error creating user from OAuth data: " + e.getMessage());
                return new RedirectView(frontendUrl + "/login?error=user_creation_failed");
            }
        }
        
        User user = userOptional.get();
        
        // Generate JWT token
        String token = jwtUtil.generateJwtToken(user);
        logger.info("JWT token generated for existing user");
        
        // Redirect to frontend with token
        return new RedirectView(frontendUrl + "/oauth/callback?token=" + token);
    }

    @GetMapping("/failure")
    public RedirectView oauthFailure() {
        logger.severe("OAuth authentication failure reported");
        // Redirect to login page with error
        return new RedirectView(frontendUrl + "/login?error=oauth_failure");
    }
} 