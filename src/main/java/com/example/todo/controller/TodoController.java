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
	
    // タスク全件取得
    @GetMapping
    public List<TodoResponse> getTodo() {
        return todoService.findAll();
    }
    
    // 日時が設定されているタスク全件取得
    @GetMapping("/scheduled")
    public List<TodoResponse> getScheduledTodos() {
        return todoService.findWithDateTime();
    }
    
    // 今日が設定されているタスク全件取得
    @GetMapping("/today")
    public List<TodoResponse> getTodayTodos() {
        return todoService.findToday();
    }
    
    // 明日が設定されているタスク全件取得
    @GetMapping("/tomorrow")
    public List<TodoResponse> getTomorrowTodos() {
        return todoService.findTomorrow();
    }
    
    // タスク一件取得
    @GetMapping("/{id}")
    public TodoResponse getTodo(@PathVariable Long id) {
        return todoService.findById(id);
    }
    
    // タスク保存
    @PostMapping
    public TodoResponse createTodo(@Valid @RequestBody TodoRequest request) { 
        return todoService.create(request.getTitle(), request.getMemo(), request.getDateTime(), request.isNotify(), request.getListId(), request.isTodayAdd(), request.isTomorrowAdd());
    }
    
    // タスク名、メモ、日時更新
    @PatchMapping("/{id}")
    public TodoResponse updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {

        return todoService.update(id, request.getTitle(), request.getMemo(), request.getDateTime(), request.isNotify());
    }
    
    // タスク削除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
    }
    
    // 各タスクの完了未完了の切り替え
    @PatchMapping("/{id}/toggle")
    public TodoResponse toggleTodo(@PathVariable Long id) {
        return todoService.toggleStatus(id);
    }
    
    // 全タスクの完了未完了切り替え
    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAllTodoStatus(@RequestParam boolean done) {
        todoService.updateAllStatus(done);
    }
    
    // タスクの通知状態の更新
    @PatchMapping("/{id}/notified")
    public TodoResponse notifiedTodo(@PathVariable Long id) {
        return todoService.markNotified(id);
    }
    
    // タスクの並び替え
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderTodos(@RequestBody List<Long> orderedIds) {
        todoService.updateOrder(orderedIds);
    }
}
