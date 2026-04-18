package com.example.todo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;

@SpringBootTest
// ServiceTestではロジックが正しく処理されていることを確認（エラー処理、データの内容確認）
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    // Repository をモック化（DB使わない）
    @MockitoBean
    private TodoRepository todoRepository;

    // ----------------------------
    // 全件取得_正常系
    // ----------------------------
    @Test
    void findAll_正常系() {
        List<Todo> mockTodos = List.of(
                createTodo(1L, "テスト1", false),
                createTodo(2L, "テスト2", false)
        );
        when(todoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))).thenReturn(mockTodos);

        // 実行
        List<TodoResponse> result = todoService.findAll();

        // 検証
        assertEquals(2, result.size());
        assertEquals("テスト1", result.get(0).getTitle());
        assertEquals("テスト2", result.get(1).getTitle());
        verify(todoRepository).findAll(Sort.by(Sort.Direction.DESC, "id")); // Repositoryが呼ばれたか確認
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void findById_正常系() {
    	Todo mockTodo = createTodo(1L, "テスト", false);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));

        // 実行
        TodoResponse result = todoService.findById(1L);

        // 検証
        assertEquals("テスト", result.getTitle());
        assertFalse(result.isDone()); 
        verify(todoRepository).findById(1L);
    }
    
    // ----------------------------
    // IDから取得_異常系
    // ----------------------------
    @Test
    void findById_IDが存在しない場合_404() {
        when(todoRepository.findById(999L))
            .thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.findById(999L);
        });
        verify(todoRepository).findById(999L);
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void create_正常系() {
        Todo mockTodo = createTodo(1L, "テスト", false);
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);
        
        // 実行
        TodoResponse result = todoService.create("テスト");

        // 検証
        verify(todoRepository, times(1)).save(any());
        assertEquals("テスト", result.getTitle());
        assertFalse(result.isDone());
    }
    
    // ----------------------------
    // 保存_異常系
    // ----------------------------
    @Test
    void create_DBエラー_400() {
        // save()が呼ばれたら例外を投げるように設定
        when(todoRepository.save(any(Todo.class))).thenThrow(new RuntimeException("DBエラー"));
        
        // 実行及び検証
        assertThrows(RuntimeException.class, () -> {
            todoService.create("テスト");
        });
        verify(todoRepository, times(1)).save(any());
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void update_正常系() {
        Todo mockTodo = createTodo(1L, "更新前", false);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);

        // 実行
        TodoResponse result = todoService.update(1L, "更新後");

        // 検証
        assertEquals("更新後", result.getTitle());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(mockTodo);
    }
    
    // ----------------------------
    // 更新_異常系
    // ----------------------------
    @Test
    void update_存在しないID_404() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.update(999L, "更新後");
        });
        verify(todoRepository).findById(999L);
        verify(todoRepository, never()).save(any());
    }
    
    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void delete_正常系() {
    	Todo mockTodo = createTodo(1L, "テスト", false);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));

        // 実行
        todoService.delete(1L);

        // 検証
        verify(todoRepository).findById(1L);
        verify(todoRepository).delete(mockTodo);
    }
    
    // ----------------------------
    // 削除_異常系
    // ----------------------------
    @Test
    void delete_IDが存在しない場合_404() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.delete(999L);
        });
        verify(todoRepository).findById(999L);
        verify(todoRepository, never()).delete(any());
    }

    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleStatus_正常系() {
        Todo mockTodo = createTodo(1L, "テスト", false);
        
        when(todoRepository.findById(1L)).thenReturn(Optional.of(mockTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(mockTodo);
        
        // 実行
        TodoResponse result = todoService.toggleStatus(1L);
        
        // 検証
        assertTrue(result.isDone());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(mockTodo);
    }
    
    // ----------------------------
    // 完了切替_異常系
    // ----------------------------
    @Test
    void toggleStatus_IDが存在しない場合_404() {
        when(todoRepository.findById(999L)) .thenReturn(Optional.empty());

        // 実行及び検証
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.toggleStatus(999L);
        });
        verify(todoRepository).findById(999L);
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
