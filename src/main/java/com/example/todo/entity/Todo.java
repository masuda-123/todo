package com.example.todo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Todo {
	
	@Id //主キー
	@GeneratedValue(strategy = GenerationType.IDENTITY) //主キーのオートインクリメント
	private Long id;
	
	@NotBlank(message = "Todoは必須です")
	@Size(max = 50, message = "Todoは50文字以内で入力してください")
	private String title;
	
	private boolean done;
	
	public Todo() {}
	
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
