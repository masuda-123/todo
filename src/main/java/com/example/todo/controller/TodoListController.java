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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.TodoList;
import com.example.todo.service.TodoListService;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lists")
public class TodoListController {

	private final TodoListService todoListService;
	private final TodoService todoService;
	
	public TodoListController(TodoListService todoListService, TodoService todoService) {
		this.todoListService = todoListService;
		this.todoService = todoService;
	}

	// 全リストの取得
    @GetMapping
    public List<TodoListResponse> findAll() {
        return todoListService.findAll();
    }
    
    // リストの作成
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoListResponse create(@Valid @RequestBody TodoList request) {
        return todoListService.create(request.getName(), request.getColor());
    }
    
    // リストの更新
    @PatchMapping("/{id}")
    public TodoListResponse updateList(
            @PathVariable Long id,
            @Valid @RequestBody TodoList request) {

        return todoListService.update(id, request.getName(), request.getColor());
    }
    
    // リストの削除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        todoListService.delete(id);
    }
    
    // リストからタスクを取得
    @GetMapping("/{id}/todos")
    public List<TodoResponse> findTodos(@PathVariable Long id) {
        return todoService.findByListId(id);
    }
    
    // リストのタスクを全て完了、未完了にする
    @PatchMapping("/{id}/todos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAllStatus(@PathVariable Long id, @RequestParam boolean done) {

        todoService.updateAllStatus(id, done);
    }
    
    // タスクの並び替え
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderTodos(@RequestBody List<Long> orderedIds) {
        todoListService.updateOrder(orderedIds);
    }
}