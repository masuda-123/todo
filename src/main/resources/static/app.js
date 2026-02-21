// HTMLの要素をjavaScriptの変数に入れる
const todoList = document.getElementById('todoList');
const addBtn = document.getElementById('addBtn');
const todoTitle = document.getElementById('todoTitle');

// Todo一覧取得
function fetchTodos() {
	// ブラウザからSpring Boot APIにGetリクエストを送る
    fetch('/todos')
    	// APIから返ってきたJSON文字列をJavascriptオブジェクトに変換
        .then(res => res.json()) // thenは成功した時に実行する処理, 受け取ったデータ=>{そのデータを使って実行する処理}
        // data = 変換後のオブジェクト（配列）
        .then(data => {
			// 前の一覧を一度消す
            todoList.innerHTML = '';
            // Todoを一つずつ<li>に表示
            data.forEach(todo => {
				// createElementで<li>要素を作成
    			const li = document.createElement('li');
    			li.id = `todo-${todo.id}`;
    			// 完了なら表示を変える
    			li.style.textDecoration = todo.done ? "line-through" : "none";
    			//<li>内のテキストにタイトルを表示
    			li.textContent = todo.title;

    			// 完了切り替えボタン
    			// createElementで<button>要素を作成
    			const toggleBtn = document.createElement('button');
    			// <button>内のテキストを設定
    			toggleBtn.textContent = '完了切替';
    			// ボタンをクリックしたら完了切替処理が行われるように設定
				toggleBtn.addEventListener('click', (e) => {
    				e.stopPropagation(); // li の onclick を止める
    				toggleTodo(todo.id);
				});
			
    			// 削除ボタン
    			// createElementで<button>要素を作成
    			const delBtn = document.createElement('button');
    			// <button>内のテキストを設定
    			delBtn.textContent = '削除';
    			// ボタンをクリックしたら削除処理が行われるように設定
    			delBtn.addEventListener('click', (e) => {
   					e.stopPropagation();
    				deleteTodo(todo.id);
				});
    			// <li>の子要素に完了切替ボタンを入れる
    			li.appendChild(toggleBtn);
    			// <li>の子要素に削除ボタンを入れる
    			li.appendChild(delBtn);
    			//todoListの子要素に<li>を入れる
    			todoList.appendChild(li);
    			
    			// --- 更新（liクリックでタイトル編集） ---
                li.addEventListener('click', () => {
                    const input = prompt("新しいタイトルを入力", todo.title)
                    // キャンセルなら処理を中断
                    if (input === null) return;
                    // trim()で前後の空白（スペース、タブ、改行）を削除
                    const newTitle = input.trim();
  					if (!newTitle) {
				        alert('タイトルを入力してください');
				        return;
				    }
				    if (newTitle.length > 50) {
				        alert('タイトルは50文字以内で入力してください');
				        return;
				    }
  					// PUTリクエストを送ってタイトルを更新
					fetch(`/todos/${todo.id}`, {
    					method: "PUT",
    					headers: { "Content-Type": "application/json" },
    					//JSON.stringifyでjavasScriptオブジェクトを文字列化する
    					body: JSON.stringify({ title: newTitle })
  					})
  					.then(res => {
                        if (!res.ok) return res.json().then(err => { throw err; });
                        return res.json();
                    })
                    .then(updated => {
                        li.textContent = updated.title;
                        li.style.textDecoration = updated.done ? "line-through" : "none";
                        li.appendChild(toggleBtn);
                        li.appendChild(delBtn);
                    })
                    .catch(err => alert(JSON.stringify(err)));
				});
			});
        })
        // エラーがある場合、アラートで表示
        .catch(err => alert(JSON.stringify(err)));
}

// 追加ボタンがクリックされた時の処理
addBtn.addEventListener('click', () => {
	// 入力欄に書かれた文字列を取得
    const title = todoTitle.value.trim();
    if (!title) {
        alert('タイトルを入力してください');
        return;
    }
    if (title.length > 50) {
        alert('タイトルは50文字以内で入力してください');
        return;
    }
    // POSTリクエストを送って新しいTodoを作成
    fetch('/todos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title }) //JSON.stringifyでjavasScriptオブジェクトを文字列化する
    })
    // HTTPステータスが200 ~ 299ならtrue
    .then(res => {　// resは返ってきたレスポンスのこと
		// // 400/404ならthrow errでcatchに飛ばす
        if (!res.ok) {
            return res.json().then(err => { throw err; });
        }
        // レスポンスをJSONに変換
        return res.json();
    })
    .then(() => {
		// 入力欄を空にする
        todoTitle.value = '';
        // 追加後に一覧を更新
        fetchTodos();
    })
    // エラーがある場合、アラートで表示
    .catch(err => alert(JSON.stringify(err))); //errをJSON.stringifyで文字列化する
});

// 削除ボタンが押された時の処理
function deleteTodo(id) {
	// 削除APIを呼ぶ
    fetch(`/todos/${id}`, { method: 'DELETE' })        
    .then(res => {
		// 400/404ならthrow errでcatchに飛ばす
		if (!res.ok) return res.json().then(err => { throw err; });
			// li要素にidを付与
            const li = document.getElementById(`todo-${id}`);
            // li要素を削除
            if (li) li.remove();
        })
        // エラーがある場合、アラートで表示
        .catch(err => alert(JSON.stringify(err)));
}

// 完了切替ボタンが押された時の処理
function toggleTodo(id) {
	// 更新APIを呼ぶ
    fetch(`/todos/${id}/toggle`, { method: 'PATCH' })
    // APIから返ってきたJSON文字列をJavascriptオブジェクトに変換
    .then(res => res.json())
    .then(updated => {
		// li要素にidを付与
		const li = document.getElementById(`todo-${id}`);
		// doneがtrueの場合は、取り消し線を引く
		if (li) li.style.textDecoration = updated.done ? "line-through" : "none";
	})
	// エラーがある場合、アラートで表示
    .catch(err => alert(JSON.stringify(err)));
}

// 初期表示
fetchTodos();
