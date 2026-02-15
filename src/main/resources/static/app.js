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
				// createElementでHTMLの要素<li>を作る
                const li = document.createElement('li');
                // <li>内のテキストにTodoタイトルを設定
                li.textContent = todo.title;
                
                // 削除ボタン
                // createElementでHTMLの要素<button>を作る
                const delBtn = document.createElement('button');
                // ボタン内のテキストを設定
                delBtn.textContent = '削除';
                // ボタンをクリックした時の処理を登録
                delBtn.onclick = () => deleteTodo(todo.id);
                // <li>内にボタンを追加
                li.appendChild(delBtn);
                // <ul>内に<li>を追加
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

// 初期表示
fetchTodos();
