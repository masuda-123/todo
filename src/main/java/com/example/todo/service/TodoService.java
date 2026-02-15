package com.example.todo.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
}

