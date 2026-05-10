package com.example.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo.entity.TodoList;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {
	
}