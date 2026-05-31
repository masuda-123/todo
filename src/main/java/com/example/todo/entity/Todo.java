package com.example.todo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Todo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "タスク名は必須です")
	@Size(max = 50, message = "タスク名は50文字以内で入力してください")
	private String title;
	
	private boolean done;
	
	@Column(name = "display_order")
	private Integer displayOrder;
	
	@Column(columnDefinition = "TEXT")
	private String memo;
	
	@Column(name = "date_time")
	private LocalDateTime dateTime;
	
	private boolean notify;
	
	@Column
	private boolean notified;
	
	@ManyToOne
	@JoinColumn(name = "list_id")
	private TodoList list;
	
	public Todo() {}
	
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    
    public boolean isNotify() { return notify; }
    public void setNotify(boolean notify) { this.notify = notify; }
    
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
    
    public TodoList getList() { return list; }
    public void setList(TodoList list) { this.list = list; }

}
