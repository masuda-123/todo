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

    			// 完了なら表示を変える
    			if (todo.done) {
					// <li>要素に取り消し線を引く
        			li.style.textDecoration = "line-through";
    			}
    			
    			//<li>内のテキストにタイトルを表示
    			li.textContent = todo.title;

    			// 完了ボタン
    			// createElementで<button>要素を作成
    			const toggleBtn = document.createElement('button');
    			// <button>内のテキストを設定
    			toggleBtn.textContent = '完了切替';
    			// ボタンをクリックしたら完了切替処理が行われるように設定
    			toggleBtn.onclick = () => toggleTodo(todo.id);
			
    			// 削除ボタン
    			// createElementで<button>要素を作成
    			const delBtn = document.createElement('button');
    			// <button>内のテキストを設定
    			delBtn.textContent = '削除';
    			// ボタンをクリックしたら削除処理が行われるように設定
    			delBtn.onclick = () => deleteTodo(todo.id);
    			// <li>の子要素に完了切替ボタンを入れる
    			li.appendChild(toggleBtn);
    			// <li>の子要素に削除ボタンを入れる
    			li.appendChild(delBtn);
    			//todoListの子要素に<li>を入れる
    			todoList.appendChild(li);
			});
        });
}

// 追加ボタンがクリックされた時の処理
addBtn.onclick = () => {
	// 入力欄に書かれた文字列を取得
    const title = todoTitle.value;
    // POSTリクエストを送って新しいTodoを作成
    fetch('/todos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title }) //JSON.stringifyでjavasScriptオブジェクトを文字列化する
    })
    // HTTPステータスが200 ~ 299ならtrue
    .then(res => {　// resは返ってきたレスポンスのこと
		// バリデーションエラーなどで400ならthrow errでcatchに飛ばす
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
    // エラーの場合、アラートで表示
    .catch(err => alert(JSON.stringify(err))); //errをJSON.stringifyで文字列化する
}

// 削除ボタンが押された時の処理
function deleteTodo(id) {
	// 削除APIを呼ぶ
    fetch(`/todos/${id}`, { method: 'DELETE' })
    	// 削除後に一覧を更新
        .then(() => fetchTodos());
}

// 完了切替ボタンが押された時の処理
function toggleTodo(id) {
	// 更新APIを呼ぶ
    fetch(`/todos/${id}/toggle`, { method: 'PATCH' })
    	// 更新後に一覧を更新
    	.then(() => fetchTodos());
}

// 初期表示
fetchTodos();
