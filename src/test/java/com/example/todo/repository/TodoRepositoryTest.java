package com.example.todo.repository;

import static org.assertj.core.api.Assertions.*;

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
    // 保存
    // ----------------------------
    @Test
    void save_正常系() {

    	// テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("テストTodo");
        todo.setDone(false);

        // 保存
        Todo saved = todoRepository.save(todo);

        // 検証
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("テストTodo");
    }

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
    void findById_存在する場合() {

    	// テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("検索テスト");
        todo.setDone(false);

        // DBに保存
        Todo saved = todoRepository.save(todo);

        // ID検索
        var result = todoRepository.findById(saved.getId());

        // 検証
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("検索テスト");
    }
    
    
    @Test
    void findById_存在しない場合() {
    	// ID検索
        var result = todoRepository.findById(999L);
        
        //検証
        assertThat(result).isEmpty();
    }
    
    // ----------------------------
    // 削除
    // ----------------------------
    @Test
    void delete_正常系() {

        // テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("削除テスト");
        todo.setDone(false);

        // DBに保存
        Todo saved = todoRepository.save(todo);

        // 削除
        todoRepository.deleteById(saved.getId());

        // 検証
        Optional<Todo> result = todoRepository.findById(saved.getId());
        assertThat(result.isEmpty());
    }
    
    // ----------------------------
    // 更新
    // ----------------------------
    @Test
    void update_正常系() {

        // テストデータを作成
        Todo todo = new Todo();
        todo.setTitle("旧タイトル");
        todo.setDone(false);
        
        // DBに保存
        Todo saved = todoRepository.save(todo);

        // 更新
        saved.setTitle("新タイトル");
        saved.setDone(true);
        todoRepository.save(saved);

        // 検証
        Todo updated = todoRepository.findById(saved.getId()).get();
        assertThat(updated.getTitle()).isEqualTo("新タイトル");
        assertThat(updated.isDone()).isTrue();
    }
}