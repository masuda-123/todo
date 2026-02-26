package com.example.todo.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // この例外が発生したら HTTPステータスを404として返すように設定
public class TodoNotFoundException extends RuntimeException {

	// Todo not found エラーのメッセージを設定
    public TodoNotFoundException(Long id) {
    	// 親クラスのコンストラクタを呼び、RuntimeExceptionに例外メッセージを設定
        super("Todo not found: " + id);
    }
}

