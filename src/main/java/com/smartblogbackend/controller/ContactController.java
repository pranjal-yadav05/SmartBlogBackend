package com.smartblogbackend.controller;

import com.smartblogbackend.model.ContactFormEntity;
import com.smartblogbackend.service.ContactFormService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactFormService contactFormService;

    @PostMapping("/contact")
    public ResponseEntity<String> submitContactForm(@RequestBody @Valid ContactFormEntity contactForm) {
        // Convert ContactForm DTO to ContactFormEntity
        ContactFormEntity contactFormEntity = new ContactFormEntity();
        contactFormEntity.setName(contactForm.getName());
        contactFormEntity.setEmail(contactForm.getEmail());
        contactFormEntity.setMessage(contactForm.getMessage());

        // Save to the database via the service
        contactFormService.saveContactForm(contactFormEntity);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Message received successfully from: " + contactForm.getName());
    }
}
