package com.example.todo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.dto.TodoListResponse;
import com.example.todo.entity.TodoList;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoListRepository;

@Service
public class TodoListService {
	private final TodoListRepository todoListRepository;
	
    public TodoListService(TodoListRepository todoListRepository) {
        this.todoListRepository = todoListRepository;
    }
    
    private TodoListResponse toResponse(TodoList todoList) {
        return new TodoListResponse(
            todoList.getId(),
            todoList.getName(),
            todoList.getColor()
        );
    }
    
    // 全件取得
    public List<TodoListResponse> findAll() {
        return todoListRepository.findAll()
        	.stream()
            .map(this::toResponse)
            .toList();
    }
    
    // 保存
    @Transactional
    public TodoListResponse create(String name, String color) {
        List<TodoList> lists = todoListRepository.findAll();
        
        // 新規Todoを保存
        TodoList newList = new TodoList();
        newList.setName(name);
        newList.setColor(color);
        todoListRepository.save(newList);

        return toResponse(newList);
    }
    
    // 更新
    public TodoListResponse update(Long id, String name, String color) {

        TodoList list = todoListRepository.findById(id).orElseThrow();

        list.setName(name);
        list.setColor(color);

        todoListRepository.save(list);

        return toResponse(todoListRepository.save(list));
    }
    
    // 削除
    public void delete(Long id) {

        TodoList list = todoListRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        todoListRepository.delete(list);
    }

}
