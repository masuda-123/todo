package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoRequest {

    @NotBlank(message = "タスク名は必須です")
    @Size(max = 50, message = "タスク名は50文字以内にしてください")
    private String title;
    
    private String memo;
    
    private Long listId;
    
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
    
    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }
}
