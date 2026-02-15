package com.example.todo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.todo.entity.Todo;
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
    public Todo save(Todo todo) {
        return todoRepository.save(todo);
    }
    
    // 1件取得
    public Todo findById(Long id) {
        return todoRepository.findById(id)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
    }
    
    // 更新
    public Todo update(Long id, Todo updatedTodo) {

        // DBから既存データ取得（なければエラー）
        Todo todo = todoRepository.findById(id)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));

        // 値を更新
        todo.setTitle(updatedTodo.getTitle());
        todo.setDone(updatedTodo.isDone());

        // 保存（UPDATE実行）
        return todoRepository.save(todo);
    }
    
    // 削除
    public void deleteTodo(Long id) {

        // 存在チェック（なければエラー）
        Todo todo = todoRepository.findById(id)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));

        // 削除
        todoRepository.delete(todo);
    }


}

