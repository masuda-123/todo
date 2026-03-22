package com.example.todo.integration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
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
        // 2件作成
        TodoRequest r1 = new TodoRequest("Todo1");
        TodoRequest r2 = new TodoRequest("Todo2");

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r1)));

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r2)));

        mockMvc.perform(MockMvcRequestBuilders.get("/todos"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("取得テスト");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        // 作成した Todo の ID を取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(MockMvcRequestBuilders.get("/todos/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("取得テスト"));
    }
    
    // ----------------------------
    // IDから取得_異常系（存在しないID）
    // ----------------------------
    @Test
    void getTodo_存在しないID() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/todos/999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void createTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("統合テスト");

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("統合テスト"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.done").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists());
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが空）
    // ----------------------------
    @Test
    void createTodo_タイトルが空文字() throws Exception {
        TodoRequest request = new TodoRequest("");

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが50文字越）
    // ----------------------------
    @Test
    void createTodo_タイトルが50文字超() throws Exception {
    	String longTitle = "あ".repeat(51);
        TodoRequest request = new TodoRequest(longTitle);

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void updateTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        TodoRequest updateRequest = new TodoRequest("更新後");

        mockMvc.perform(MockMvcRequestBuilders.put("/todos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("更新後"));
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが空）
    // ----------------------------
    @Test
    void updateTodo_タイトルが空() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        TodoRequest updateRequest = new TodoRequest("");

        mockMvc.perform(MockMvcRequestBuilders.put("/todos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが50文字超）
    // ----------------------------
    @Test
    void updateTodo_タイトルが50文字超() throws Exception {
    	TodoRequest request = new TodoRequest("更新前");
    	
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        
        String longTitle = "あ".repeat(51);
        TodoRequest updateRequest = new TodoRequest(longTitle);

        mockMvc.perform(MockMvcRequestBuilders.put("/todos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("削除テスト");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/" + id))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 削除後に GET すると 404
        mockMvc.perform(MockMvcRequestBuilders.get("/todos/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    // ----------------------------
    // 削除_異常系（存在しないID）
    // ----------------------------
    @Test
    void deleteTodo_存在しないID() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/" + 999))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleDone_正常系() throws Exception {
        TodoRequest request = new TodoRequest("トグルテスト");

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // PATCH で done を true に更新
        Map<String, Object> patch = Map.of("done", true);
        String patchJson = objectMapper.writeValueAsString(patch);

        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/" + id + "/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.done").value(true));
    }
    
    // ----------------------------
    // 完了切替_異常系（存在しないID）
    // ----------------------------
    @Test
    void toggleDone_異常系() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/" + 999 + "/toggle"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
