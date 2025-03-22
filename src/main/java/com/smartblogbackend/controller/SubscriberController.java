package com.smartblogbackend.controller;

import com.smartblogbackend.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
public class SubscriberController {

    @Autowired
    private SubscriberService subscriberService;

    @PostMapping("/subscribe")
    public ResponseEntity<Object> subscribe(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean subscribed = subscriberService.subscribe(email);
        
        if (subscribed) {
            response.put("success", true);
            response.put("message", "Successfully subscribed to the newsletter");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Email is already subscribed");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Object> unsubscribe(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean unsubscribed = subscriberService.unsubscribe(email);
        
        if (unsubscribed) {
            response.put("success", true);
            response.put("message", "Successfully unsubscribed from the newsletter");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Email is not subscribed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Test endpoint to trigger newsletter sending manually (admin only in production)
    @PostMapping("/send-test-newsletter")
    public ResponseEntity<Object> sendTestNewsletter() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            subscriberService.sendWeeklyNewsletter();
            response.put("success", true);
            response.put("message", "Newsletter sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send newsletter: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 