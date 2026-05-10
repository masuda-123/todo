package com.example.todo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    // API返却用
    private TodoResponse toResponse(Todo todo) {
        return new TodoResponse(
            todo.getId(),
            todo.getTitle(),
            todo.isDone(),
            todo.getMemo()
        );
    }

    // 全件取得
    public List<TodoResponse> findAll() {
        return todoRepository.findAllByOrderByDisplayOrderAsc()
        	.stream()
            .map(this::toResponse)
            .toList();
    }

    // 保存
    @Transactional
    public TodoResponse create(String title) {
        List<Todo> todos = todoRepository.findAllByOrderByDisplayOrderAsc();

        // 既存Todoの順番を1つ後ろにする
        for (Todo todo : todos) {
            todo.setDisplayOrder(todo.getDisplayOrder() + 1);
        }
        
        // 新規Todoを保存
        Todo newTodo = new Todo();
        newTodo.setTitle(title);
        newTodo.setDone(false);
        newTodo.setDisplayOrder(0);
        todoRepository.save(newTodo);

        return toResponse(newTodo);
    }
    
    // 1件取得
    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        return toResponse(todo);
    }
    
    // 更新
    public TodoResponse update(Long id, String title, String memo) {

        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        todo.setTitle(title);
        todo.setMemo(memo);

        return toResponse(todoRepository.save(todo));
    }
    
    // 削除
    public void delete(Long id) {

        // 存在チェック（なければエラー）
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        todoRepository.delete(todo);
    }
    
    // 完了、未完了の切り替え
    public TodoResponse toggleStatus(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        todo.setDone(!todo.isDone()); // true ⇄ false 反転
        return toResponse(todoRepository.save(todo));

    }
    
    // 並び替え
    @Transactional
    public void updateOrder(List<Long> orderedIds) {

        for (int i = 0; i < orderedIds.size(); i++) {

            Todo todo = todoRepository.findById(orderedIds.get(i))
                    .orElseThrow();

            todo.setDisplayOrder(i);
        }
    }
}

