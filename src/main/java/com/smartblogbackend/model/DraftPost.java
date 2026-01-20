package com.smartblogbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "draft_posts")
public class DraftPost extends BasePost {
    
    public DraftPost() {
        super();
    }

    public DraftPost(Long id, String title, String content, User author, LocalDateTime createdAt) {
        this.setId(id);
        this.setTitle(title);
        this.setContent(content);
        this.setAuthor(author);
        this.setCreatedAt(createdAt);
    }
}
