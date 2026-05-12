package com.example.todo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.TodoListResponse;
import com.example.todo.entity.TodoList;
import com.example.todo.service.TodoListService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lists")
public class TodoListController {

	private final TodoListService todoListService;
	
	public TodoListController(TodoListService todoListService) {
		this.todoListService = todoListService;
	}

    @GetMapping
    public List<TodoListResponse> findAll() {
        return todoListService.findAll();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoListResponse create(@Valid @RequestBody TodoList request) {
        return todoListService.create(request.getName(), request.getColor());
    }
    
    @PatchMapping("/{id}")
    public TodoListResponse updateList(
            @PathVariable Long id,
            @Valid @RequestBody TodoList request) {

        return todoListService.update(id, request.getName(), request.getColor());
    }
    
    // タスク削除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        todoListService.delete(id);
    }
}