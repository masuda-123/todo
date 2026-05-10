package com.example.todo.dto;

import com.example.todo.entity.Todo;

public class TodoResponse {

    private Long id;
    private String title;
    private boolean done;
    private String memo;
    private String listColor;

    public TodoResponse(Long id, String title, boolean done, String memo, String listColor) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.memo = memo;
        this.listColor = listColor;
    }
    
    public static TodoResponse from(Todo todo){
    	return new TodoResponse(todo.getId(), todo.getTitle(), todo.isDone(), todo.getMemo(), todo.getList().getColor());
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }
    public String getMemo() { return memo; }
    public String getListColor() { return listColor; }
}