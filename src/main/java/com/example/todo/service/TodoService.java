package com.example.todo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoList;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoListRepository;
import com.example.todo.repository.TodoRepository;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoListRepository todoListRepository;

    public TodoService(TodoRepository todoRepository, TodoListRepository todoListRepository) {
        this.todoRepository = todoRepository;
        this.todoListRepository = todoListRepository;
    }
    
    // API返却用
    private TodoResponse toResponse(Todo todo) {
        return new TodoResponse(
            todo.getId(),
            todo.getTitle(),
            todo.isDone(),
            todo.getMemo(),
            todo.getDateTime(),
            todo.isNotified(),
            todo.getList().getColor()
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
    public TodoResponse create(String title, Long listId) {
        List<Todo> todos = todoRepository.findAllByOrderByDisplayOrderAsc();
        TodoList list = todoListRepository.findById(listId).orElseThrow();

        // 既存Todoの順番を1つ後ろにする
        for (Todo todo : todos) {
            todo.setDisplayOrder(todo.getDisplayOrder() + 1);
        }
        
        // 新規Todoを保存
        Todo newTodo = new Todo();
        newTodo.setTitle(title);
        newTodo.setDone(false);
        newTodo.setDisplayOrder(0);
        newTodo.setList(list);
        newTodo.setNotified(false);
        todoRepository.save(newTodo);

        return toResponse(newTodo);
    }
    
    // リストからタスクを取得
    public List<TodoResponse> findByListId(Long listId) {
    	
        return todoRepository
                .findByListIdOrderByDisplayOrderAsc(listId)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }
    
    // 1件取得
    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        return toResponse(todo);
    }
    
    // 1件のタスクの更新
    public TodoResponse update(Long id, String title, String memo, LocalDateTime dateTime) {

        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        todo.setTitle(title);
        todo.setMemo(memo);
        todo.setDateTime(dateTime);

        return toResponse(todoRepository.save(todo));
    }
    
    // 1件のタスクの削除
    public void delete(Long id) {

        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));

        todoRepository.delete(todo);
    }
    
    // 1件のタスクの完了、未完了の切り替え
    public TodoResponse toggleStatus(Long id) {
    	
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        todo.setDone(!todo.isDone()); // true ⇄ false 反転
        return toResponse(todoRepository.save(todo));

    }
    
    
    // 全てのタスクの完了、未完了切り替え
    @Transactional
    public void updateAllStatus(Long listId, boolean done) {
        List<Todo> todos = todoRepository.findByListIdOrderByDisplayOrderAsc(listId);

        for (Todo todo : todos) {
            todo.setDone(done);
        }
    }
    
    // タスクの通知状態の更新
    public TodoResponse markNotified(Long id) {
    	
        Todo todo = todoRepository.findById(id)
        	.orElseThrow(() -> new TodoNotFoundException(id));
        todo.setNotified(true);
        return toResponse(todoRepository.save(todo));

    }
    
    // タスクの並び替え
    @Transactional
    public void updateOrder(List<Long> orderedIds) {
    	
        for (int i = 0; i < orderedIds.size(); i++) {

            Todo todo = todoRepository.findById(orderedIds.get(i))
                    .orElseThrow();

            todo.setDisplayOrder(i);
        }
    }
}

