package com.example.todo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;

// @SpringBootTestは、Spring Bootを起動してテストするという意味
@SpringBootTest
class TodoServiceTest {

	// SpringがTodoServiceを自動生成して注入
    @Autowired
    private TodoService todoService;

    // Repository をモック化（DB使わない）
    // モックは自分で動きを決められる偽物オブジェクト
    @MockitoBean
    private TodoRepository todoRepository;

    // ----------------------------
    // 全件取得
    // ----------------------------
    @Test
    void findAll_正常系() { //メソッド名_期待値
        // モックデータを作成
        List<Todo> mockTodos = List.of(
                createTodo(1L, "テスト1", false),
                createTodo(2L, "テスト2", true)
        );
        // RepositoryのfindAll()が呼ばれたら、mockTodosを返すように設定
        when(todoRepository.findAll()).thenReturn(mockTodos);

        // 実行
        List<TodoResponse> result = todoService.findAll();

        // 検証
        assertEquals(2, result.size()); //2件データが返っていることを確認
        assertEquals("テスト1", result.get(0).getTitle()); //1件目のタイトルが正しいことを確認
    }

    // ----------------------------
    // 保存
    // ----------------------------
    @Test
    void save_正常系() {
    	// DBに保存するデータ
        Todo savedTodo = createTodo(1L, "新規Todo", false);
        // save()が呼ばれたら saveTodo()を返すように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        
        // 実行
        TodoResponse result = todoService.save("新規Todo");

        // 検証
        assertEquals("新規Todo", result.getTitle()); //タイトルが正しいことを確認
        assertFalse(result.isDone()); //doneがfalseであることを確認
    }

    // ----------------------------
    // 完了切替
    // ----------------------------
    @Test
    void toggleTodo_完了から未完了へ() {
    	// DBに保存するデータ
        Todo todo = createTodo(1L, "テスト", true);
        
        // ID=1を探したら、このTodoを返すように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        // save()が呼ばれたら、このTodoを返すように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        // 実行
        TodoResponse result = todoService.toggleTodo(1L);
        
        // 検証
        assertFalse(result.isDone()); // true → false に変わる
    }
    
    @Test
    void toggleTodo_IDが存在しない場合_例外() {
        // Repositoryが何も返さないように設定
        when(todoRepository.findById(1L))
            .thenReturn(Optional.empty());

        // 例外が発生するか確認
        assertThrows(RuntimeException.class, () -> {
            todoService.toggleTodo(1L);
        });
    }
    
    @Test
    void save_DBエラー時_例外() {
    	// Repositoryが保存時に、エラーを返すように設定
        when(todoRepository.save(any(Todo.class)))
            .thenThrow(new RuntimeException("DB error"));
        
        // 例外が発生するか確認
        assertThrows(RuntimeException.class, () -> {
            todoService.save("テスト");
        });
    }

    // ----------------------------
    // テスト用ヘルパー
    // ----------------------------
    private Todo createTodo(Long id, String title, boolean done) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setDone(done);
        return todo;
    }
}
