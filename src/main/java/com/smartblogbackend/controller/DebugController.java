package com.smartblogbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Debug controller - only for development purposes
 * These endpoints should be disabled or removed in production
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = Logger.getLogger(DebugController.class.getName());

    @GetMapping("/auth-status")
    public Map<String, Object> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("authType", authentication != null ? authentication.getClass().getSimpleName() : "None");
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            
            response.put("oauth2Provider", oauthToken.getAuthorizedClientRegistrationId());
            
            Map<String, Object> userInfo = new HashMap<>();
            oauth2User.getAttributes().forEach((key, value) -> {
                // Don't include sensitive information
                if (!key.contains("access_token") && !key.contains("refresh_token")) {
                    userInfo.put(key, value);
                }
            });
            
            response.put("userInfo", userInfo);
        } else if (authentication != null) {
            response.put("principal", authentication.getPrincipal().toString());
            response.put("authorities", authentication.getAuthorities());
        }
        
        return response;
    }
    
    @GetMapping("/session-info")
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                response.put("sessionExists", true);
                response.put("sessionId", session.getId());
                response.put("creationTime", session.getCreationTime());
                response.put("lastAccessedTime", session.getLastAccessedTime());
                response.put("maxInactiveInterval", session.getMaxInactiveInterval());
                
                // Get session attributes
                Map<String, Object> attributes = new HashMap<>();
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String name = attributeNames.nextElement();
                    // Don't include sensitive information
                    if (!name.toLowerCase().contains("token") && !name.toLowerCase().contains("password")) {
                        attributes.put(name, session.getAttribute(name));
                    } else {
                        attributes.put(name, "[REDACTED]");
                    }
                }
                response.put("attributes", attributes);
            } else {
                response.put("sessionExists", false);
            }
        } catch (Exception e) {
            response.put("error", "Failed to get session info: " + e.getMessage());
            logger.severe("Error getting session info: " + e.getMessage());
        }
        
        return response;
    }
} 