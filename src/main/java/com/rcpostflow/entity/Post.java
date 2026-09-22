package com.rcpostflow.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.EnumType;
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @NotBlank(message = "タイトルは必須です")
    private String title;

    @NotBlank(message = "本文は必須です")
    private String body;

    private String url;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "channels")
    private String channels;

    public Long getId() {
    return id;
    }

    public void setId(Long id) {
    this.id = id;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
 
    public String getChannels() {
    return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.PENDING;
    
    public PostStatus getStatus() {
    return status;
    }

    public void setStatus(PostStatus status) {
    this.status = status;
    }
}