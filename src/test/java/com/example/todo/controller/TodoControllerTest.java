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
import com.example.todo.service.TodoService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TodoController.class) // コントローラーだけ起動
@AutoConfigureMockMvc(addFilters = false) // Security 無効化
// ControllerTestではHTTPとして正しく動くかを確認
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
    void getAllTodos_正常系() throws Exception {
    	// テストデータを作成
        List<TodoResponse> mockResponses = List.of(
                new TodoResponse(1L, "テスト1", false, LocalDateTime.now()),
                new TodoResponse(2L, "テスト2", true, LocalDateTime.now())
        );

        // finaAll()が呼ばれたら、mockResponseを返すように設定
        when(todoService.findAll()).thenReturn(mockResponses);

        // "/todos" に対してgetリクエストを送る
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$[0].title").value("テスト1")) // 1つ目のデータのタイトルが正しいことを確認
                .andExpect(jsonPath("$[1].done").value(true)); // 2つ目のデータのdoneが正しいことを確認
    }
    
    // ----------------------------
    // GET /todos/{id}
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
    	// テストデータを作成
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false, LocalDateTime.now());

        // findByIdが呼ばれたら、mockTodoを返すように設定
        when(todoService.findById(1L)).thenReturn(mockResponse);

        // "/todos/1" にgetリクエストを送る
        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("テスト"));// タイトルが正しいことを確認
    }

    // ----------------------------
    // POST /todos
    // ----------------------------
    @Test
    void postTodo_正常系() throws Exception {
    	// テストデータを作成
        TodoRequest request = new TodoRequest("テスト");
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false, LocalDateTime.now());
        
        // createが呼ばれたら、mockResponseを返すように設定
        when(todoService.create(anyString())).thenReturn(mockResponse);
        // requestをjson文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(json))  // リクエストボディに入力データを入れる
                .andExpect(status().isOk())  // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("テスト")) // タイトルが正しいことを確認
                .andExpect(jsonPath("$.done").value(false)); // doneが正しいことを確認
        
        verify(todoService).create("テスト"); // todoService.create("テスト")が呼ばれることを確認
    }
    
    // ----------------------------
    // POST /todos
    // タイトルが空
    // ----------------------------
    @Test
    void postTodo_タイトル空文字_400() throws Exception {
    	// テストデータ作成
    	TodoRequest request = new TodoRequest("");
    	// requestをjson文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(json))  // リクエストボディに入力データを入れる
        		.andExpect(status().isBadRequest()); // バリデーションエラーが発生することを確認
    }
    
    // ----------------------------
    // POST /todos
    // タイトルが50文字超え
    // ----------------------------
    @Test
    void postTodo_タイトルが50文字超_400() throws Exception {
    	// テストデータ作成
        String longTitle = "あ".repeat(51);
        // requestをjson文字列に変換
        TodoRequest request = new TodoRequest(longTitle);
        
        // requestオブジェクトをJSON文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(json)) // リクエストボディに入力データを入れる
               	.andExpect(status().isBadRequest()); // バリデーションエラーが発生することを確認
    }
    
    // ----------------------------
    // PUT /todos/1
    // 正常系
    // ----------------------------
    @Test
    void putTodo_正常系() throws Exception {
    	// テストデータを作成
        TodoRequest request = new TodoRequest("テスト");
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false, LocalDateTime.now());
        
        // updateが呼ばれたら、mockResponseを返すように設定
        when(todoService.update(eq(1L), any(TodoRequest.class))).thenReturn(mockResponse);
        // requestをjson文字列に変換
        String json = objectMapper.writeValueAsString(request);

        // "/todos/1" にputリクエストを送る
        mockMvc.perform(put("/todos/1")
                .contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(json))  // リクエストボディに入力データを入れる
                .andExpect(status().isOk())  // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("テスト")); // タイトルが正しいことを確認
        
        verify(todoService).update(eq(1L), argThat(req -> //todoService.update()が呼ばれたことを確認
        	req.getTitle().equals("テスト")));
    }

    // ----------------------------
    // DELETE /todos/{id}
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
    	// deleteTodoを呼んでも、何も返さないように設定
        doNothing().when(todoService).delete(1L);

        // "/todos/1" にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/1"))
        	.andExpect(status().isNoContent()); // HTTPステータスが204であることを確認
        
        verify(todoService, times(1)).delete(1L); //todoService.delete(1L)が呼ばれたことを確認
    }

    // ----------------------------
    // PATCH /todos/{id}/toggle
    // ----------------------------
    @Test
    void patchTodo_正常系() throws Exception {
    	// テストデータを作成
        TodoResponse mockResponse = new TodoResponse(1L, "テスト1", true, LocalDateTime.now());

        // toggleTodo(1L)が呼ばれたら、mockResponseを返すように設定
        when(todoService.toggleStatus(1L)).thenReturn(mockResponse);

        // "/todos/1/toggle" にpatchリクエストを送る
        mockMvc.perform(patch("/todos/1/toggle")
                .contentType(MediaType.APPLICATION_JSON))  // JSONでやり取りすることを指定
                .andExpect(status().isOk())	// HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.done").value(true)); // データのdoneの値が正しいことを確認
        
        verify(todoService, times(1)).toggleStatus(1L); // todoService.toggleStatus(1L)が呼ばれたことを確認
    }
}
