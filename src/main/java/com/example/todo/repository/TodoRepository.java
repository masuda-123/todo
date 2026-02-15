package com.example.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo.entity.Todo;

// JPAを使ってCategories操作を自動生成
public interface TodoRepository extends JpaRepository<Todo, Long> {

}
