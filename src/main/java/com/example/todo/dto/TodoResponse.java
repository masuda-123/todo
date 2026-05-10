package com.example.todo.dto;

public class TodoResponse {

    private Long id;
    private String title;
    private boolean done;
    private String memo;

    public TodoResponse(Long id, String title, boolean done, String memo) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public String getMemo() { return memo; }
}