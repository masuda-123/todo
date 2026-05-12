package com.example.todo.dto;

import com.example.todo.entity.TodoList;

public class TodoListResponse {

    private Long id;
    private String name;
    private String color;

    public TodoListResponse(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
    
    public static TodoListResponse from(TodoList todoList){
    	return new TodoListResponse(todoList.getId(), todoList.getName(), todoList.getColor());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
}
