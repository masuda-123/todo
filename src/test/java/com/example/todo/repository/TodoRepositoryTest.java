package com.example.todo.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.todo.entity.Todo;

// Repository + JPA + テスト用DBだけ起動
@DataJpaTest
// RepositoryTestでは、DBの処理が正しいことを確認
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    // ----------------------------
    // 全件取得
    // ----------------------------
    @Test
    void findAll_正常系() {

    	// テストデータを作成
        Todo todo1 = new Todo();
        todo1.setTitle("テスト1");
        todo1.setDone(false);

        Todo todo2 = new Todo();
        todo2.setTitle("テスト2");
        todo2.setDone(true);

        // DBに保存
        todoRepository.saveAll(List.of(todo1, todo2));

        // 全件取得
        List<Todo> result = todoRepository.findAll();

        // 検証
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("テスト1");
        assertThat(result.get(1).getTitle()).isEqualTo("テスト2");
        
    }

    // ----------------------------
    // ID検索
    // ----------------------------
    @Test
    void findById_正常系() {

    	// テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("テスト");
        todo.setDone(false);

        // DBに保存
        Todo saved = todoRepository.save(todo);

        // ID検索
        var result = todoRepository.findById(saved.getId());

        // 検証
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("テスト");
        assertFalse(result.get().isDone());
    }
    
    // ----------------------------
    // 保存
    // ----------------------------
    @Test
    void save_正常系() {

    	// テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("テスト");
        todo.setDone(true);

        // 保存
        Todo saved = todoRepository.save(todo);

        // 検証
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("テスト");
        assertTrue(saved.isDone());
    }
    
    // ----------------------------
    // 削除
    // ----------------------------
    @Test
    void delete_正常系() {

        // テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("テスト");
        todo.setDone(false);

        // DBに保存
        Todo saved = todoRepository.save(todo);

        // 削除
        todoRepository.delete(saved);

        // 検証
        Optional<Todo> result = todoRepository.findById(saved.getId());
        assertThat(result.isEmpty());
    }
}