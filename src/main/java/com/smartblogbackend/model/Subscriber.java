package com.smartblogbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean active;

    private LocalDateTime subscribedAt;
    
    private LocalDateTime lastEmailSent;

    // Default constructor
    public Subscriber() {
        this.active = true;
        this.subscribedAt = LocalDateTime.now();
    }

    // Constructor with email
    public Subscriber(String email) {
        this.email = email;
        this.active = true;
        this.subscribedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public LocalDateTime getLastEmailSent() {
        return lastEmailSent;
    }

    public void setLastEmailSent(LocalDateTime lastEmailSent) {
        this.lastEmailSent = lastEmailSent;
    }
} 