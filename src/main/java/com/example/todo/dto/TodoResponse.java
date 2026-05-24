package com.example.todo.dto;

import java.time.LocalDateTime;

import com.example.todo.entity.Todo;

public class TodoResponse {

    private Long id;
    private String title;
    private boolean done;
    private String memo;
    private LocalDateTime dateTime;
    private boolean notified;
    private String listColor;
    private Long listId;

    public TodoResponse(Long id, String title, boolean done, String memo, LocalDateTime dateTime, boolean notified, String listColor, Long listId) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.memo = memo;
        this.dateTime = dateTime;
        this.notified = notified;
        this.listColor = listColor;
        this.listId = listId;
    }
    
    public static TodoResponse from(Todo todo){
    	return new TodoResponse(todo.getId(), todo.getTitle(), todo.isDone(), todo.getMemo(), todo.getDateTime(), todo.isNotified(), todo.getList().getColor(), todo.getList().getId());
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public String getMemo() { return memo; }
    public LocalDateTime getDateTime() { return dateTime; }
    public boolean isNotified() { return notified; }
    public String getListColor() { return listColor; }
    public Long getListId() { return listId; }
}