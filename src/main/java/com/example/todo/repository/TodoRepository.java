package com.example.todo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo.entity.Todo;

// JPAを使ってTodoデータを操作
public interface TodoRepository extends JpaRepository<Todo, Long> {
	List<Todo> findAllByOrderByDisplayOrderAsc();
	List<Todo> findAllByOrderByIdDesc();
	List<Todo> findByListIdOrderByDisplayOrderAsc(Long listId);
	List<Todo> findByListIsNullOrderByDisplayOrderAsc();
	List<Todo> findByDateTimeIsNotNullOrderByDisplayOrderAsc();
	List<Todo> findByDateTimeGreaterThanEqualAndDateTimeLessThanOrderByDisplayOrderAsc(
	        LocalDateTime start,
	        LocalDateTime end
	);
}
