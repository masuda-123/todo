package com.example.todo.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 無効化
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TodoService todoService;

    // ----------------------------
    // GET /todos
    // ----------------------------
    @Test
    void getAllTodos_正常系() throws Exception {
        List<TodoResponse> mockResponses = List.of(
                new TodoResponse(1L, "テスト1", false),
                new TodoResponse(2L, "テスト2", true)
        );

        when(todoService.findAll()).thenReturn(mockResponses);

        // "/todos" に対してgetリクエストを送る
        mockMvc.perform(get("/todos"))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$[0].title").value("テスト1"))
        	.andExpect(jsonPath("$[1].done").value(true));
    }
    
    // ----------------------------
    // GET /todos/{id}
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false);

        when(todoService.findById(1L)).thenReturn(mockResponse);

        // "/todos/1" にgetリクエストを送る
        mockMvc.perform(get("/todos/1"))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("テスト"));
    }

    // ----------------------------
    // POST /todos
    // ----------------------------
    @Test
    void postTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false);
        
        when(todoService.create(anyString())).thenReturn(mockResponse);

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("テスト"))
        	.andExpect(jsonPath("$.done").value(false));
        
        verify(todoService).create("テスト");
    }
    
    // ----------------------------
    // POST /todos
    // タイトルが空
    // ----------------------------
    @Test
    void postTodo_タイトル空文字_400() throws Exception {
    	TodoRequest request = new TodoRequest("");

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isBadRequest());
    }
    
    // ----------------------------
    // POST /todos
    // タイトルが50文字超え
    // ----------------------------
    @Test
    void postTodo_タイトルが50文字超_400() throws Exception {
        String longTitle = "あ".repeat(51);
        TodoRequest request = new TodoRequest(longTitle);

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isBadRequest());
    }
    
    // ----------------------------
    // PATCH /todos/1
    // 正常系
    // ----------------------------
    @Test
    void patchTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");
        TodoResponse mockResponse = new TodoResponse(1L, "テスト", false);
        
        when(todoService.update(1L, request.getTitle())).thenReturn(mockResponse);

        // "/todos/1" にputリクエストを送る
        mockMvc.perform(patch("/todos/1")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("テスト"));
        
        verify(todoService).update(1L, request.getTitle());
    }

    // ----------------------------
    // DELETE /todos/{id}
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
        doNothing().when(todoService).delete(1L);

        // "/todos/1" にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/1"))
        	.andExpect(status().isNoContent());
        
        verify(todoService, times(1)).delete(1L);
    }

    // ----------------------------
    // PATCH /todos/{id}/toggle
    // ----------------------------
    @Test
    void patchTodoStatus_正常系() throws Exception {
        TodoResponse mockResponse = new TodoResponse(1L, "テスト1", true);

        when(todoService.toggleStatus(1L)).thenReturn(mockResponse);

        // "/todos/1/toggle" にpatchリクエストを送る
        mockMvc.perform(patch("/todos/1/toggle")
        	.contentType(MediaType.APPLICATION_JSON))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.done").value(true));
        
        verify(todoService, times(1)).toggleStatus(1L);
    }
}
