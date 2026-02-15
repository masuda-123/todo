package com.example.todo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.entity.Todo;
import com.example.todo.service.TodoService;

@RestController
@RequestMapping("/todos")
public class TodoController {
	
	private final TodoService todoService;
	
	public TodoController(TodoService todoService) {
		this.todoService = todoService;
	}
	
    // Todo一覧取得
    @GetMapping
    public List<Todo> getTodos() {
        return todoService.findAll();
    }
    
    // Todo保存
    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        return todoService.save(todo);
    }
    
    // Todo一件取得
    @GetMapping("/{id}")
    public Todo getTodo(@PathVariable Long id) { // @PathVariable = URLから動的な値を受け取る
        return todoService.findById(id);
    }
}
