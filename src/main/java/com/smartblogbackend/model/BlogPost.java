package com.smartblogbackend.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "blog_posts"
)
public class BlogPost {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    private String title;

    @Column(columnDefinition = "TEXT") // ✅ Store longer content
    private String content;

    private String category;
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User author;

    @Column(name = "image_url")
    private String imageUrl;


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public User getAuthor() {
        return this.author;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setAuthor(final User author) {
        this.author = author;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BlogPost() {
    }

    public BlogPost(final Long id, final String title, final String content, final User author, final LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}