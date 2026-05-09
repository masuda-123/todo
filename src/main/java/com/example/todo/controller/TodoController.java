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

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/todos")
public class TodoController {
	
	private final TodoService todoService;
	
	public TodoController(TodoService todoService) {
		this.todoService = todoService;
	}
	
    // タスク一覧取得
    @GetMapping
    public List<TodoResponse> getTodos() {
        return todoService.findAll();
    }
    
    // タスク一件取得
    @GetMapping("/{id}")
    public TodoResponse getTodo(@PathVariable Long id) {
        return todoService.findById(id);
    }
    
    // タスク保存
    @PostMapping
    public TodoResponse createTodo(@Valid @RequestBody TodoRequest request) { 
        return todoService.create(request.getTitle());
    }
    
    // タスク名更新
    @PatchMapping("/{id}")
    public TodoResponse updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {

        return todoService.update(id, request.getTitle());
    }
    
    // タスク削除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
    }
    
    // タスクの完了未完了の切り替え
    @PatchMapping("/{id}/toggle")
    public TodoResponse toggleTodo(@PathVariable Long id) {
        return todoService.toggleStatus(id);
    }
    
    // タスクの並び替え
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderTodos(@RequestBody List<Long> orderedIds) {
        todoService.updateOrder(orderedIds);
    }
}
