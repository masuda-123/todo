package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoListRequest {

    @NotBlank(message = "リスト名は必須です")
    @Size(max = 50, message = "リスト名は50文字以内にしてください")
    private String name;
    
    private String color;
    
    // テスト用のコンストラクタ
    public TodoListRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getColor() {
    	return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }

}
