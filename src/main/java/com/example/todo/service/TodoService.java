package com.example.todo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
    
    // Entity → DTO 変換（privateメソッド）
    private TodoResponse toResponse(Todo todo) {
        return new TodoResponse(
            todo.getId(),
            todo.getTitle(),
            todo.isDone(),
            todo.getCreatedAt()
        );
    }

    // 全件取得
    public List<TodoResponse> findAll() {
        return todoRepository.findAll()
        		.stream()
                .map(this::toResponse)
                .toList();
    }

    // 保存
    public TodoResponse save(String title) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setTitle(title);
        todo.setDone(false);
        todo.setCreatedAt(LocalDateTime.now());
        return toResponse(todoRepository.save(todo));
    }
    
    // 1件取得
    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id)); // idが見つからない場合は onElseThrowが実行される
        return toResponse(todo);
    }
    
    // 更新
    public TodoResponse update(Long id, TodoRequest request) {

        // DBから既存データ取得（なければエラー）
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        // 値を更新
        todo.setTitle(request.getTitle());

        // 保存
        return toResponse(todoRepository.save(todo));
    }
    
    // 削除
    public void deleteTodo(Long id) {

        // 存在チェック（なければエラー）
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        // 削除
        todoRepository.delete(todo);
    }
    
    // 完了、未完了の切り替え
    public TodoResponse toggleTodo(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        todo.setDone(!todo.isDone()); // true ⇄ false 反転
        return toResponse(todoRepository.save(todo));

    }
}

