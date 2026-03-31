package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoRequest {

    @NotBlank(message = "タスク名は必須です")
    @Size(max = 50, message = "タスク名は50文字以内にしてください")
    private String title;
    
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
}
