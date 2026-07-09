package com.example.todo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoRequest {

    @NotBlank(message = "タスク名は必須です")
    @Size(max = 50, message = "タスク名は50文字以内にしてください")
    private String title;
    
    private String memo;
    
    private LocalDateTime dateTime;
    
    private boolean notify;
    
    private Long listId;
    
    private boolean todayAdd;
    
    private boolean tomorrowAdd;
    
    // テスト用のコンストラクタ
    public TodoRequest(String title) {
        this.title = title;
    }

	public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMemo() {
    	return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public LocalDateTime getDateTime() {
    	return dateTime;
    }
    
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    
    public boolean isNotify() {
    	return notify;
    }
    
    public void setNotify(boolean notify) {
    	this.notify = notify;
    }
    
    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }
    
    public boolean isTodayAdd() {
        return todayAdd;
    }

    public void setTodayAdd(boolean todayAdd) {
        this.todayAdd = todayAdd;
    }
    
    public boolean isTomorrowAdd() {
        return tomorrowAdd;
    }

    public void setTomorrowAdd(boolean tomorrowAdd) {
        this.tomorrowAdd = tomorrowAdd;
    }
}
