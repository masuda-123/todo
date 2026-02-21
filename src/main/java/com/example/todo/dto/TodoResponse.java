package com.example.todo.dto;

import java.time.LocalDateTime;

public class TodoResponse {

    private Long id;
    private String title;
    private boolean done;
    private LocalDateTime createdAt;

    public TodoResponse(Long id, String title, boolean done, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}