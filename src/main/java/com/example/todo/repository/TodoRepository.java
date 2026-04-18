package com.example.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo.entity.Todo;

// JPAを使ってTodoデータを操作
public interface TodoRepository extends JpaRepository<Todo, Long> {

}
