package com.example.todo.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.service.TodoService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TodoController.class) //TodoControllerクラスだけを対象にweb層だけを実行
@AutoConfigureMockMvc(addFilters = false) // Security 無効化
class TodoControllerTest {

	// Spring が提供するテスト用の 仮想的な Web クライアントを注入
    @Autowired
    private MockMvc mockMvc;
    
    // JavaのオブジェクトとJSONを相互に変換するためのクラスを注入
    @Autowired
    private ObjectMapper objectMapper;

    // Serviceクラスをもモック化
    @MockitoBean
    private TodoService todoService;

    // ----------------------------
    // GET /todos
    // ----------------------------
    @Test
    void getTodos_正常系() throws Exception {
    	// テスト用のモックデータを作成
        List<TodoResponse> mockTodos = List.of(
                new TodoResponse(1L, "テスト1", false, LocalDateTime.now()),
                new TodoResponse(2L, "テスト2", true, LocalDateTime.now())
        );

        // finaAll()が呼ばれたら、モックデータを返すように設定
        when(todoService.findAll()).thenReturn(mockTodos);

        // /todos に対してgetリクエストを送る
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$[0].title").value("テスト1")) // 1つ目のデータのタイトルが正しいことを確認
                .andExpect(jsonPath("$[1].done").value(true)); // 2つ目のデータのdoneが正しいことを確認
    }

    // ----------------------------
    // POST /todos
    // ----------------------------
    @Test
    void createTodo_正常系() throws Exception {
    	// テスト用のモックデータを作成
        TodoResponse response = new TodoResponse(1L, "新規Todo", false, LocalDateTime.now());

        // save("新規Todo"）が呼ばれたら、モックデータを返すように設定
        when(todoService.save("新規Todo")).thenReturn(response);

        // /todos にpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content("{\"title\":\"新規Todo\"}")) // リクエストボディとして渡す
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("新規Todo")) // 1つ目のデータのタイトルが正しいことを確認
                .andExpect(jsonPath("$.done").value(false)); // 2つ目のデータのdoneが正しいことを確認
    }

    // ----------------------------
    // GET /todos/{id}
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
    	// テスト用のモックデータを作成
        TodoResponse response = new TodoResponse(1L, "テスト1", false, LocalDateTime.now());

        // findByIdが呼ばれたら、モックデータを返すように設定
        when(todoService.findById(1L)).thenReturn(response);

        // /todos/1 にgetリクエストを送る
        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("テスト1")); // データのタイトルが正しいことを確認
    }

    // ----------------------------
    // PUT /todos/{id}
    // ----------------------------
    @Test
    void updateTodo_正常系() throws Exception {
    	// テスト用のモックデータを作成
        TodoResponse response = new TodoResponse(1L, "更新後Todo", false, LocalDateTime.now());

        // updateが呼ばれたら、モックデータを渡すように設定
        when(todoService.update(eq(1L), any())).thenReturn(response);

        // /todos/1 にputリクエストを送る
        mockMvc.perform(put("/todos/1")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content("{\"title\":\"更新後Todo\"}"))  // リクエストボディとして渡す
                .andExpect(status().isOk())  // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("更新後Todo")); // データのタイトルが正しいことを確認
    }

    // ----------------------------
    // DELETE /todos/{id}
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
    	// deleteTodoを呼んでも、何も返さないように設定
        doNothing().when(todoService).deleteTodo(1L);

        // /todos/1 にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/1"))
        		.andExpect(status().isNoContent()); // HTTPステータスが204であることを確認

        verify(todoService, times(1)).deleteTodo(1L); // todoServiceが1回呼ばれたことを確認
    }

    // ----------------------------
    // PATCH /todos/{id}/toggle
    // ----------------------------
    @Test
    void toggleTodo_正常系() throws Exception {
    	// テスト用のモックデータを作成
        TodoResponse response = new TodoResponse(1L, "テスト1", true, LocalDateTime.now());

        // toggleTodo(1L)が呼ばれたら、モックデータを返すように設定
        when(todoService.toggleTodo(1L)).thenReturn(response);

        // /todos/1/toggle にpatchリクエストを送る
        mockMvc.perform(patch("/todos/1/toggle")
                .contentType(MediaType.APPLICATION_JSON))  // JSONでやり取りすることを指定
                .andExpect(status().isOk())	// HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.done").value(true)); // データのdoneの値が正しいことを確認
    }
    
    // ----------------------------
    // 異常系
    // ----------------------------
    @Test
    void getTodo_ID存在しない場合_404() throws Exception {
    	// findByIdが呼ばれたら、例外を投げるように設定
        when(todoService.findById(1L)).thenThrow(new TodoNotFoundException(1L));

        // /togos/1 にgetリクエストを送る
        mockMvc.perform(get("/todos/1"))
               .andExpect(status().isNotFound()); // 404 Not Found になることを確認
    }
    
    @Test
    void updateTodo_タイトル空文字_400() throws Exception {
    	// タイトルが空文字のrequestオブジェクトを作成
        TodoRequest request = new TodoRequest("");
        // requestオブジェクトをJSON文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // /togos/1 にputリクエストを送る
        mockMvc.perform(put("/todos/1")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(json)) // リクエストボディに JSON 文字列を設定
               .andExpect(status().isBadRequest()); // バリデーションエラーが発生することを確認
    }
    
    @Test
    void updateTodo_タイトルが50文字超_400() throws Exception {
    	// タイトルが51文字のrequestオブジェクトを作成
        String longTitle = "あ".repeat(51);
        TodoRequest request = new TodoRequest(longTitle);
        
        // requestオブジェクトをJSON文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // /togos/1 にputリクエストを送る
        mockMvc.perform(put("/todos/1")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(json)) // リクエストボディに JSON 文字列を設定
               .andExpect(status().isBadRequest()); // バリデーションエラーが発生することを確認
    }
    
    @Test
    void deleteTodo_ID存在しない場合_404() throws Exception {
    	// deleteTodoが呼ばれたら、例外を投げるように設定
    	doThrow(new TodoNotFoundException(1L)).when(todoService).deleteTodo(1L);

     // /togos/1 にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/1"))
               .andExpect(status().isNotFound()); // 404 Not Found になることを確認
    }
}
