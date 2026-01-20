package com.smartblogbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
public class BlogPost extends BasePost {
    
    private boolean published = true;

    public BlogPost() {
        super();
    }

    public BlogPost(Long id, String title, String content, User author, LocalDateTime createdAt) {
        this.setId(id);
        this.setTitle(title);
        this.setContent(content);
        this.setAuthor(author);
        this.setCreatedAt(createdAt);
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}