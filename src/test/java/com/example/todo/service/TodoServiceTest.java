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
                createTodo(2L, "テスト2", false)
        );
        // RepositoryのfindAll()が呼ばれたら、mockTodosを使うように設定
        when(todoRepository.findAll()).thenReturn(mockTodos);

        // 実行
        List<TodoResponse> result = todoService.findAll();

        // 検証
        assertEquals(2, result.size()); //2件データが返っていることを確認
        assertEquals("テスト1", result.get(0).getTitle()); //1件目のタイトルが正しいことを確認
        assertEquals("テスト2", result.get(1).getTitle()); //2件目のタイトルが正しいことを確認
        verify(todoRepository).findAll(); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void findById_正常系() {
        // モックデータを作成
    	Todo mockTodo = createTodo(1L, "テスト", false);
        // RepositoryのfindById()が呼ばれたら、mockTodoを使うように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));

        // 実行
        TodoResponse result = todoService.findById(1L);

        // 検証
        assertEquals("テスト", result.getTitle()); //タイトルが正しいことを確認
        assertFalse(result.isDone()); //doneがfalseであることを確認
        verify(todoRepository).findById(1L); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // IDから取得_異常系
    // ----------------------------
    @Test
    void findById_IDが存在しない場合() {
        // findById(999L)が呼ばれたら何も使わないように設定
        when(todoRepository.findById(999L))
            .thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> { // 例外が発生するか確認
            todoService.findById(999L);
        });
        verify(todoRepository).findById(999L); // Repositoryが呼ばれたか確認
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void create_正常系() {
    	// モックデータを作成
        Todo mockTodo = createTodo(1L, "テスト", false);
        // Repository.save()が呼ばれたら mockTodoを使うように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);
        
        // 実行
        TodoResponse result = todoService.create("テスト");

        // 検証
        verify(todoRepository, times(1)).save(any()); // Repositoryが1回呼ばれたか確認
        assertEquals("テスト", result.getTitle()); //タイトルが正しいことを確認
        assertFalse(result.isDone()); //doneがfalseであることを確認
    }
    
    // ----------------------------
    // 保存_異常系
    // ----------------------------
    @Test
    void create_DBエラー() {
        // save()が呼ばれたら例外を投げるように設定
        when(todoRepository.save(any(Todo.class))).thenThrow(new RuntimeException("DBエラー"));
        
        // 実行及び検証
        assertThrows(RuntimeException.class, () -> { // 例外が発生するか確認
            todoService.create("テスト");
        });
        verify(todoRepository, times(1)).save(any()); // Repositoryが1回呼ばれたか確認
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void update_正常系() {
        // モックデータ作成
        Todo mockTodo = createTodo(1L, "更新前", false);

        // findById(1L) が呼ばれたら mockTodoを使うように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));
        // save が呼ばれたら mockTodoを使うように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);

        // 実行
        TodoResponse result = todoService.update(1L, "更新後");

        // 検証
        assertEquals("更新後", result.getTitle()); //タイトルが正しいことを確認
        verify(todoRepository).findById(1L); // Repositoryが呼ばれたか確認
        verify(todoRepository).save(mockTodo); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // 更新_異常系
    // ----------------------------
    @Test
    void update_存在しないID() {
    	// findById(999L)が呼ばれたら何も使わないように設定
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> { // 例外が発生するか確認
            todoService.update(999L, "更新後");
        });
        verify(todoRepository).findById(999L); // Repositoryが呼ばれることを確認
        verify(todoRepository, never()).save(any()); // Repositoryが呼ばれないことを確認
    }
    
    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void delete_正常系() {
        // モックデータを作成
    	Todo mockTodo = createTodo(1L, "テスト", false);

    	// findById(1L) が呼ばれたら mockTodoを使うように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));

        // 実行
        todoService.delete(1L);

        // 検証
        verify(todoRepository).findById(1L); // Repositoryが呼ばれたか確認
        verify(todoRepository).delete(mockTodo); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // 削除_異常系
    // ----------------------------
    @Test
    void delete_IDが存在しない場合() {
    	// findById(999L)が呼ばれたら何も使わないように設定
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> { // 例外が発生するか確認
            todoService.delete(999L);
        });
        verify(todoRepository).findById(999L); // Repositoryが呼ばれたか確認
        verify(todoRepository, never()).delete(any()); // Repositoryが呼ばれないことを確認
    }

    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleStatus_正常系() {
        // モックデータ作成
        Todo mockTodo = createTodo(1L, "テスト", false);
        
        // findById(1L) が呼ばれたら mockTodo を使うように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));
        // save が呼ばれたら mockTodo を使うように設定
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);
        
        // 実行
        TodoResponse result = todoService.toggleStatus(1L);
        
        // 検証
        assertTrue(result.isDone()); // false → true に変わる
        verify(todoRepository).findById(1L); // Repositoryが呼ばれたか確認
        verify(todoRepository).save(mockTodo); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // 完了切替_異常系
    // ----------------------------
    @Test
    void toggleStatus_IDが存在しない場合() {
    	// findById(999L)が呼ばれたら何も使わないように設定
        when(todoRepository.findById(999L)) .thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> { // 例外が発生するか確認
            todoService.toggleStatus(999L);
        });
        verify(todoRepository).findById(999L); // Repositoryが呼ばれたか確認
        verify(todoRepository, never()).save(any()); // Repositoryが呼ばれないことを確認
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
