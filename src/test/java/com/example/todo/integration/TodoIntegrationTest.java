package com.example.todo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.dto.TodoRequest;

import tools.jackson.databind.ObjectMapper;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TodoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    // ----------------------------
    // 全件取得_正常系
    // ----------------------------
    @Test
    void getAllTodos_正常系() throws Exception {
        TodoRequest request1 = new TodoRequest("テスト1");
        TodoRequest request2 = new TodoRequest("テスト2");

        // 取得用のタスクを作成
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)));
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)));
        
        // タスクを全件取得する
        mockMvc.perform(get("/todos"))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.length()").value(2))
        	.andExpect(jsonPath("$[0].title").value("テスト2"))
        	.andExpect(jsonPath("$[0].done").value(false))
        	.andExpect(jsonPath("$[1].title").value("テスト1"))
        	.andExpect(jsonPath("$[1].done").value(false));
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // 取得用のタスクを作成
        String response = mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したデータからidを取得する
        Long id = objectMapper.readTree(response).get("id").asLong();

        // idからタスクを取得する
        mockMvc.perform(get("/todos/" + id))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("テスト"))
        	.andExpect(jsonPath("$.done").value(false));
    }
    
    // ----------------------------
    // IDから取得_異常系（存在しないID）
    // ----------------------------
    @Test
    void getTodo_存在しないID_404() throws Exception {
    	// 存在しないIDを指定してタスクを取得する
        mockMvc.perform(get("/todos/999"))
        	.andExpect(status().isNotFound());
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void createTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // タスクを作成
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("テスト"))
        	.andExpect(jsonPath("$.done").value(false));
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが空）
    // ----------------------------
    @Test
    void createTodo_タイトルが空文字_400() throws Exception {
        TodoRequest request = new TodoRequest("");

        // タスクを作成
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isBadRequest());
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが50文字越）
    // ----------------------------
    @Test
    void createTodo_タイトルが50文字超_400() throws Exception {
    	String longTitle = "あ".repeat(51);
        TodoRequest request = new TodoRequest(longTitle);
        
        // タスクを作成
        mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andExpect(status().isBadRequest());
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void updateTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        // 更新用のタスクを作成
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したタスクからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();
        
        TodoRequest updateRequest = new TodoRequest("更新後");

        // idを指定してタスクを更新
        mockMvc.perform(patch("/todos/" + id)
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(updateRequest)))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.title").value("更新後"))
        	.andExpect(jsonPath("$.done").value(false));
    }
    
    // ----------------------------
    // 更新_異常系（存在しないID）
    // ----------------------------
    @Test
    void updateTodo_存在しないID_404() throws Exception {
    	TodoRequest updateRequest = new TodoRequest("更新後");
    	
        // 存在しないidを指定してタスク名を更新
        mockMvc.perform(patch("/todos/999")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(updateRequest)))
        	.andExpect(status().isNotFound());
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが空）
    // ----------------------------
    @Test
    void updateTodo_タイトルが空_400() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        // 更新用のタスクを作成
        String response = mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したタスクからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        TodoRequest updateRequest = new TodoRequest("");

        // idを指定してタスク名を更新
        mockMvc.perform(patch("/todos/" + id)
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(updateRequest)))
        	.andExpect(status().isBadRequest());
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが50文字超）
    // ----------------------------
    @Test
    void updateTodo_タイトルが50文字超_400() throws Exception {
    	TodoRequest request = new TodoRequest("更新前");
    	
        // 更新用のタスクを作成
        String response = mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したタスクからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();
        
        String longTitle = "あ".repeat(51);
        TodoRequest updateRequest = new TodoRequest(longTitle);

        // idを指定してタスク名を更新
        mockMvc.perform(patch("/todos/" + id)
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(updateRequest)))
        	.andExpect(status().isBadRequest());
    }

    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // 削除用のタスクを作成
        String response = mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したタスクからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        // idを指定してタスクを削除
        mockMvc.perform(delete("/todos/" + id))
                .andExpect(status().isNoContent());

        // 削除したタスクを取得
        mockMvc.perform(get("/todos/" + id))
                .andExpect(status().isNotFound());
    }
    
    // ----------------------------
    // 削除_異常系（存在しないID）
    // ----------------------------
    @Test
    void deleteTodo_存在しないID_404() throws Exception {
    	// idを指定してタスクを削除
        mockMvc.perform(delete("/todos/" + 999))
                .andExpect(status().isNotFound());
    }
    
    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleDone_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // 完了切り替え用のタスクを作成
        String response = mockMvc.perform(post("/todos")
        	.contentType(MediaType.APPLICATION_JSON)
        	.content(objectMapper.writeValueAsString(request)))
        	.andReturn().getResponse().getContentAsString();

        // 作成したタスクからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        // idを指定してタスクの完了状態を切り替える
        mockMvc.perform(patch("/todos/" + id + "/toggle")
        	.contentType(MediaType.APPLICATION_JSON))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.done").value(true))
        	.andExpect(jsonPath("$.title").value("テスト"));
    }
    
    // ----------------------------
    // 完了切替_異常系（存在しないID）
    // ----------------------------
    @Test
    void toggleDone_異常系_404() throws Exception {
    	// idを指定してタスクの完了状態を切り替える
        mockMvc.perform(patch("/todos/" + 999 + "/toggle"))
                .andExpect(status().isNotFound());
    }
}
