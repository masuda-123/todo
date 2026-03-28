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

import tools.jackson.databind.JsonNode;
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

        // "/todos" に対してpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(objectMapper.writeValueAsString(request1))); // リクエストボディにrequestをjson文字列に変換して渡す
        
        // "/todos" に対してpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
                .content(objectMapper.writeValueAsString(request2))); // リクエストボディにrequestをjson文字列に変換して渡す
        
        // "/todos" に対してgetリクエストを送る
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.length()").value(2)) // 件数を確認
                .andExpect(jsonPath("$[0].title").value("テスト1")) // 1件目のタイトル確認
                .andExpect(jsonPath("$[0].done").value(false))   // 1件目の完了状態確認
                .andExpect(jsonPath("$[1].title").value("テスト2")) // 2件目のタイトル確認
                .andExpect(jsonPath("$[1].done").value(false));  // 2件目の完了状態確認
    }
    
    // ----------------------------
    // IDから取得_正常系
    // ----------------------------
    @Test
    void getTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        String response = mockMvc.perform(post("/todos") // "/todos" に対してpostリクエストを送る
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        // "/todos/{id}"にgetリクエストを送る
        mockMvc.perform(get("/todos/" + id))
        		.andExpect(status().isOk()) // HTTPステータスが200であることを確認
        		.andExpect(jsonPath("$.title").value("テスト")) // タイトルが正しいことを確認
        		.andExpect(jsonPath("$.done").value(false)); // 完了状態がfalseであることを確認
    }
    
    // ----------------------------
    // IDから取得_異常系（存在しないID）
    // ----------------------------
    @Test
    void getTodo_存在しないID() throws Exception {
    	// "/todos/999"にgetリクエストを送る
        mockMvc.perform(get("/todos/999"))
                .andExpect(status().isNotFound()); // HTTPステータスが404であることを確認
    }

    // ----------------------------
    // 保存_正常系
    // ----------------------------
    @Test
    void createTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andExpect(status().isOk()) // HTTPステータスが200であることを確認
        		.andExpect(jsonPath("$.title").value("テスト")) // タイトルが正しいことを確認
        		.andExpect(jsonPath("$.done").value(false)) // 完了状態がfalseであることを確認
        		.andExpect(jsonPath("$.createdAt").exists()); // 作成日が存在していることを確認
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが空）
    // ----------------------------
    @Test
    void createTodo_タイトルが空文字() throws Exception {
        TodoRequest request = new TodoRequest("");

        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andExpect(status().isBadRequest()); // HTTPステータスが400であることを確認
    }
    
    // ----------------------------
    // 保存_異常系（タイトルが50文字越）
    // ----------------------------
    @Test
    void createTodo_タイトルが50文字超() throws Exception {
    	String longTitle = "あ".repeat(51);
        TodoRequest request = new TodoRequest(longTitle);
        
        // "/todos" にpostリクエストを送る
        mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
                .andExpect(status().isBadRequest()); // HTTPステータスが400であることを確認
    }
    
    // ----------------------------
    // 更新_正常系
    // ----------------------------
    @Test
    void updateTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        // "/todos" にpostリクエストを送る
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidとcreatedAtを取得
        JsonNode json = objectMapper.readTree(response);
        Long id = objectMapper.readTree(response).get("id").asLong();
        String createdAtBefore = json.get("createdAt").asString();

        TodoRequest updateRequest = new TodoRequest("更新後");

        // "/todos/{id}" にputリクエストを送る
        mockMvc.perform(put("/todos/" + id)
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(updateRequest))) // リクエストボディにupdateRequestをjson文字列に変換して渡す
                .andExpect(status().isOk()) // HTTPステータスが200であることを確認
                .andExpect(jsonPath("$.title").value("更新後")) // タイトルが変わっていることを確認
                .andExpect(jsonPath("$.done").value(false)) // 完了状態が変わっていないことを確認
                .andExpect(jsonPath("$.createdAt").value(createdAtBefore)); // 作成日が変わっていないことを確認
    }
    
    // ----------------------------
    // 更新_異常系（存在しないID）
    // ----------------------------
    @Test
    void updateTodo_存在しないID() throws Exception {
    	TodoRequest updateRequest = new TodoRequest("更新後");
    	
        // "/todos/999" にputリクエストを送る
        mockMvc.perform(put("/todos/999")
				.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
				.content(objectMapper.writeValueAsString(updateRequest))) // リクエストボディにuprateRequestをjson文字列に変換して渡す
        		.andExpect(status().isNotFound()); // HTTPステータスが404であることを確認
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが空）
    // ----------------------------
    @Test
    void updateTodo_タイトルが空() throws Exception {
        TodoRequest request = new TodoRequest("更新前");

        // "/todos" にpostリクエストを送る
        String response = mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        TodoRequest updateRequest = new TodoRequest("");

        // "/todos/{id}" にputリクエストを送る
        mockMvc.perform(put("/todos/" + id)
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(updateRequest))) // リクエストボディにupdateRequestをjson文字列に変換して渡す
        		.andExpect(status().isBadRequest()); // HTTPステータスが400であることを確認
    }
    
    // ----------------------------
    // 更新_異常系（タイトルが50文字超）
    // ----------------------------
    @Test
    void updateTodo_タイトルが50文字超() throws Exception {
    	TodoRequest request = new TodoRequest("更新前");
    	
        // "/todos" にpostリクエストを送る
        String response = mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();
        
        String longTitle = "あ".repeat(51);
        TodoRequest updateRequest = new TodoRequest(longTitle);

        // "/todos/{id}" にputリクエストを送る
        mockMvc.perform(put("/todos/" + id)
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(updateRequest))) // リクエストボディにupdateRequestをjson文字列に変換して渡す
        		.andExpect(status().isBadRequest()); // HTTPステータスが400であることを確認
    }

    // ----------------------------
    // 削除_正常系
    // ----------------------------
    @Test
    void deleteTodo_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // "/todos" にpostリクエストを送る
        String response = mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidを取得
        Long id = objectMapper.readTree(response).get("id").asLong();

        // "/todos/{id}" にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/" + id))
                .andExpect(status().isNoContent()); // HTTPステータスが204であることを確認

        // "/todos/{id}" にgetリクエストを送る
        mockMvc.perform(get("/todos/" + id))
                .andExpect(status().isNotFound()); // HTTPステータスが404であることを確認
    }
    
    // ----------------------------
    // 削除_異常系（存在しないID）
    // ----------------------------
    @Test
    void deleteTodo_存在しないID() throws Exception {
    	// "/todos/{id}" にdeleteリクエストを送る
        mockMvc.perform(delete("/todos/" + 999))
                .andExpect(status().isNotFound()); // HTTPステータスが404であることを確認
    }
    
    // ----------------------------
    // 完了切替_正常系
    // ----------------------------
    @Test
    void toggleDone_正常系() throws Exception {
        TodoRequest request = new TodoRequest("テスト");

        // "/todos" にpostリクエストを送る
        String response = mockMvc.perform(post("/todos")
        		.contentType(MediaType.APPLICATION_JSON) // JSONでやり取りすることを指定
        		.content(objectMapper.writeValueAsString(request))) // リクエストボディにrequestをjson文字列に変換して渡す
        		.andReturn().getResponse().getContentAsString(); // リクエストのレスポンスを文字列化

        // レスポンスからidとcreatedAtを取得
        JsonNode json = objectMapper.readTree(response);
        Long id = objectMapper.readTree(response).get("id").asLong();
        String createdAtBefore = json.get("createdAt").asString();

        // "/todos/{id}/toggle" にpatchリクエストを送る
        mockMvc.perform(patch("/todos/" + id + "/toggle")
        		.contentType(MediaType.APPLICATION_JSON)) // JSONでやり取りすることを指定
        		.andExpect(status().isOk()) // HTTPステータスが200であることを確認
        		.andExpect(jsonPath("$.done").value(true)) // 完了状態がfalseからtrueに変わっていることを確認
        		.andExpect(jsonPath("$.title").value("テスト")) // タイトルが変わっていないことを確認
        		.andExpect(jsonPath("$.createdAt").value(createdAtBefore)); // 作成日が変わっていないことを確認
        		
    }
    
    // ----------------------------
    // 完了切替_異常系（存在しないID）
    // ----------------------------
    @Test
    void toggleDone_異常系() throws Exception {
        // "/todos/999/toggle" にpatchリクエストを送る
        mockMvc.perform(patch("/todos/" + 999 + "/toggle"))
                .andExpect(status().isNotFound()); // HTTPステータスが404であることを確認
    }
}
