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

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;

// アプリ全体を起動
@SpringBootTest
// ServiceTestではロジックが正しく処理されていることを確認（エラー処理、データの内容確認）
class TodoServiceTest {

	// SpringがTodoServiceを自動生成して注入
    @Autowired
    private TodoService todoService;

    // Repository をモック化（DB使わない）
    // モックは自分で動きを決められる偽物オブジェクト
    @MockitoBean
    private TodoRepository todoRepository;

    // ----------------------------
    // 全件取得_正常系
    // ----------------------------
    @Test
    void findAll_正常系() {
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
        
        // Repositoryが呼ばれたか確認
        verify(todoRepository).findAll();
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void create_正常系() {
    	// DBに保存するデータ
        Todo savedTodo = createTodo(1L, "新規Todo", false);
        // save()が呼ばれたら saveTodo()を返すように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        
        // 実行
        TodoResponse result = todoService.create("新規Todo");

        // 検証
        assertEquals("新規Todo", result.getTitle()); //タイトルが正しいことを確認
        assertFalse(result.isDone()); //doneがfalseであることを確認
        
        // Repositoryが1回呼ばれたか確認
        verify(todoRepository, times(1)).save(any());
    }
    
    // ----------------------------
    // 保存_異常系
    // ----------------------------
    @Test
    void create_DBエラー() {
    	// Repositoryが保存時に、エラーを返すように設定
        when(todoRepository.save(any(Todo.class)))
            .thenThrow(new RuntimeException("DB error"));
        
        // 例外が発生するか確認
        assertThrows(RuntimeException.class, () -> {
            todoService.create("テスト");
        });
        
        // Repositoryが1回呼ばれたか確認
        verify(todoRepository, times(1)).save(any());
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void findById_正常系() {
        // モックデータを作成
    	Todo todo = createTodo(1L, "テスト", true);
        // RepositoryのfindById()が呼ばれたら、mockTodosを返すように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        // 実行
        TodoResponse result = todoService.findById(1L);

        // 検証
        assertEquals("テスト", result.getTitle()); //タイトルが正しいことを確認
        
        // Repositoryが呼ばれたか確認
        verify(todoRepository).findById(1L);
    }
    
    // ----------------------------
    // IDから取得_異常系
    // ----------------------------
    @Test
    void findById_IDが存在しない場合() {
        // Repositoryが何も返さないように設定
        when(todoRepository.findById(999L))
            .thenReturn(Optional.empty());

        // 例外が発生するか確認
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.findById(999L);
        });
        
        // Repositoryが呼ばれたか確認
        verify(todoRepository).findById(999L);
    }
    
    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void delete_正常系() {
        // モックデータを作成
    	Todo todo = createTodo(1L, "テスト", true);

        // findById が呼ばれたら todo を返すように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        // 実行
        todoService.delete(1L);

        // Repositoryが呼ばれたか確認
        verify(todoRepository).findById(1L);
        verify(todoRepository).delete(todo);
    }
    
    // ----------------------------
    // 削除_異常系
    // ----------------------------
    @Test
    void delete_IDが存在しない場合() {
        // Repositoryが何も返さないように設定
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 例外が発生するか確認
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.delete(999L);
        });
        
        // repositoryが呼ばれたか確認
        verify(todoRepository).findById(999L);
        // repositoryが呼ばれないことを確認
        verify(todoRepository, never()).delete(any());
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void update_正常系() {
        // モックデータ作成
        Todo todo = createTodo(1L, "更新前", true);
        Todo updatedTodo = createTodo(1L, "更新後", true);

        // findById が呼ばれたら既存データを返す
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        // save が呼ばれたら更新後のTodoを返す
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        // 実行
        TodoRequest request = new TodoRequest("更新後");
        TodoResponse result = todoService.update(1L, request);

        // 検証
        assertEquals("更新後", result.getTitle()); //タイトルが正しいことを確認
        
        // repositoryが呼ばれたか確認
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(todo);
    }
    
    // ----------------------------
    // 更新_異常系
    // ----------------------------
    @Test
    void update_存在しないID() {
    	// Repositoryが何も返さないように設定
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        TodoRequest request = new TodoRequest("更新後");

     // 例外が発生するか確認
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.update(999L, request);
        });

        // Repositoryが呼ばれることを確認
        verify(todoRepository).findById(999L);
        // repositoryが呼ばれないことを確認
        verify(todoRepository, never()).save(any());
    }

    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleStatus_正常系() {
    	// DBに保存するデータ
        Todo todo = createTodo(1L, "テスト", true);
        
        // ID=1を探したら、このTodoを返すように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        // save()が呼ばれたら、このTodoを返すように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        // 実行
        TodoResponse result = todoService.toggleStatus(1L);
        
        // 検証
        assertFalse(result.isDone()); // true → false に変わる
        
        // Repositoryが呼ばれたか確認
        verify(todoRepository).findById(1L);
        verify(todoRepository, times(1)).save(any());
    }
    
    // ----------------------------
    // 完了切替_異常系
    // ----------------------------
    @Test
    void toggleStatus_IDが存在しない場合() {
        // Repositoryが何も返さないように設定
        when(todoRepository.findById(1L)) .thenReturn(Optional.empty());

        // 例外が発生するか確認
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.toggleStatus(1L);
        });
        
        // repositoryが呼ばれたか確認
        verify(todoRepository).findById(1L);
        // repositoryが呼ばれないことを確認
        verify(todoRepository, never()).save(any());
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
