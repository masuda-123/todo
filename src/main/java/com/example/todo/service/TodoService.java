package com.example.todo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.todo.dto.TodoRequest;
import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // 全件取得
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    // 保存
    public Todo save(String title) {
        Todo todo = new Todo();
        todo.setTitle(title);
        if(!todo.isDone()) {
        	todo.setDone(false);
        }
        return todoRepository.save(todo);
    }
    
    // 1件取得
    public Todo findById(Long id) {
        return todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id)); // idが見つからない場合は onElseThrowが実行される
    }
    
    // 更新
    public Todo update(Long id, TodoRequest request) {

        // DBから既存データ取得（なければエラー）
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        // 値を更新
        todo.setTitle(request.getTitle());

        // 保存
        return todoRepository.save(todo);
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
    public Todo toggleTodo(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        todo.setDone(!todo.isDone()); // true ⇄ false 反転
        return todoRepository.save(todo);
    }
}

