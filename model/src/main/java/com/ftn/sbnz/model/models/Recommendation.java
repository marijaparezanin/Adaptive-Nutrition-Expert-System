package com.ftn.sbnz.model.models;

import java.time.LocalDateTime;

public class Recommendation {

    private String type;
    private String message;
    private Long userId;
    private LocalDateTime createdAt;

    public Recommendation() {
    }

    public Recommendation(String type, String message, Long userId, LocalDateTime createdAt) {
        this.type = type;
        this.message = message;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
