package com.smartblogbackend.controller;

import com.smartblogbackend.model.User;
import com.smartblogbackend.service.UserService;
import com.smartblogbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Optional;

@Controller
public class OAuthRedirectController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/login/oauth2/code/google")
    public RedirectView handleGoogleRedirect() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return new RedirectView(frontendUrl + "/login?error=auth_failed");
        }
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        if (email == null) {
            return new RedirectView(frontendUrl + "/login?error=email_missing");
        }
        
        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isEmpty()) {
            return new RedirectView(frontendUrl + "/login?error=user_not_found");
        }
        
        User user = userOptional.get();
        String token = jwtUtil.generateJwtToken(user);
        
        return new RedirectView(frontendUrl + "/oauth/callback?token=" + token);
    }
} 