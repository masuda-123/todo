package com.example.todo.exception;

public class TodoNotFoundException extends RuntimeException {

	// Todo not found エラーのメッセージを設定
    public TodoNotFoundException(Long id) {
    	// 親クラスのコンストラクタを呼び、RuntimeExceptionに例外メッセージを設定
        super("Todo not found: " + id);
    }

}

