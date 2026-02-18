package com.example.todo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.entity.Todo;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

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
    public Todo createTodo(@Valid @RequestBody Todo todo) { // @Valid = Todoオブジェクトのバリデーションを実行
        return todoService.save(todo);
    }
    
    // Todo一件取得
    @GetMapping("/{id}")
    public Todo getTodo(@PathVariable Long id) { // @PathVariable = URLから動的な値を受け取る
        return todoService.findById(id);
    }
    
    // Todo更新
    @PutMapping("/{id}")
    public Todo updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody Todo todo) { // @RequestBody  = リクエストボディ（JSON）から値を受け取る

        return todoService.update(id, todo);
    }
    
    // Todo削除
    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
    }
    
    @PatchMapping("/{id}/toggle")
    public Todo toggleTodo(@PathVariable Long id) {
        return todoService.toggleTodo(id);
    }

}
