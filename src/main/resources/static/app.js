// ----------------------------
// HTML要素
// ----------------------------
const todoList = document.getElementById('todoList');
const addBtn = document.getElementById('addBtn');
const todoTitle = document.getElementById('todoTitle');

// ----------------------------
// ボタン作成関数
// ----------------------------
function createToggleBtn(todo) {
	// createElementで<button>要素を作成
    const btn = document.createElement('button');
    btn.textContent = '完了切替';
    // ボタンをクリックしたら完了切り替え処理が行われるように設定
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleTodo(todo.id);
    });
    return btn;
}

function createDeleteBtn(todo) {
	// createElementで<button>要素を作成
    const btn = document.createElement('button');
    btn.textContent = '削除';
    // ボタンをクリックしたら削除処理が行われるように設定
    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        deleteTodo(todo.id);
    });
    return btn;
}

// ----------------------------
// UI部分更新関数
// ----------------------------
function updateTodoUI(updatedTodo) {
	// 特定のli要素をidを指定して取得
    const li = document.getElementById(`todo-${updatedTodo.id}`);
    // li要素がない場合はこの処理を抜ける
    if (!li) return;
    // 更新後のタイトルをliの文字列に設定する
    li.textContent = updatedTodo.title;
    // 更新後のdoneがtrueの場合は取り消し線を引く
    li.style.textDecoration = updatedTodo.done ? 'line-through' : 'none';

    // <li>の子要素に完了切替ボタンを入れる
    li.appendChild(createToggleBtn(updatedTodo));
    // <li>の子要素に削除ボタンを入れる
    li.appendChild(createDeleteBtn(updatedTodo));
}


// ----------------------------
// Todo取得＆描画
// ----------------------------
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
                // 完了なら取り消し線を表示
                li.textContent = todo.title;
                li.style.textDecoration = todo.done ? "line-through" : "none";

                // <li>の子要素に完了切替ボタンを入れる
                li.appendChild(createToggleBtn(todo));
                // <li>の子要素に削除ボタンを入れる
                li.appendChild(createDeleteBtn(todo));

                // liクリックで編集
                li.addEventListener('click', () => editTodo(todo));
                //todoListの子要素に<li>を入れる
                todoList.appendChild(li);
            });
        })
        // エラーがある場合、アラートで表示
        .catch(err => alert(JSON.stringify(err))); //errをJSON.stringifyで文字列化する
}

// ----------------------------
// バリデーション関数
// ----------------------------
function validateTitle(input) {
	// キャンセルが押された場合、nullを返す
	if (input === null) return null;
	// 入力欄に書かれた文字列を取得
    const t = input?.trim(); // trim()で前後の空白（スペース、タブ、改行）を削除
    if (!t) {
        alert('タイトルを入力してください');
        return null;
    }
    if (t.length > 50) {
        alert('タイトルは50文字以内で入力してください');
        return null;
    }
    return t;
}

// ----------------------------
// 追加
// ----------------------------
addBtn.addEventListener('click', () => {
	// 入力欄に書かれた文字列を取得し、バリデーションを実行
    const title = validateTitle(todoTitle.value);
    // キャンセルが押せれた場合、もしくはから文字だった場合処理を中断
    if (!title) return;
    // POSTリクエストを送ってTodoデータを追加する
    fetch('/todos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title })
    })
    .then(res => {
		// HTTP statusが400もしくは404ならthrow errでcatchに飛ばす
        if (!res.ok) return res.json().then(err => { throw err; });
        // APIから返ってきたJSON文字列をJavascriptオブジェクトに変換
        return res.json();
    })
    .then(() => {
		// 入力欄を空にする
        todoTitle.value = '';
        // Todoの一覧を取得、表示
        fetchTodos();
    })
    // エラーがある場合、アラートで表示
    .catch(err => alert(JSON.stringify(err)));
});

// ----------------------------
// 編集関数
// ----------------------------
function editTodo(todo) {
	// 入力されたデータを取得
	const newTitle = validateTitle(prompt("新しいタイトルを入力", todo.title));
	// キャンセルが押せれた場合、もしくはから文字だった場合処理を中断
    if (!newTitle) return;
	// PUTリクエストを送ってTodoデータを更新する
    fetch(`/todos/${todo.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: newTitle })
    })
    .then(res => {
		// HTTP statusが400もしくは404ならthrow errでcatchに飛ばす
        if (!res.ok) return res.json().then(err => { throw err; });
        // APIから返ってきたJSON文字列をJavascriptオブジェクトに変換
        return res.json();
    })
    .then(updated => updateTodoUI(updated))
    // エラーがある場合、アラートで表示
    .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 削除
// ----------------------------
function deleteTodo(id) {
    fetch(`/todos/${id}`, { method: 'DELETE' })
        .then(res => {
			// HTTP statusが400もしくは404ならthrow errでcatchに飛ばす
            if (!res.ok) return res.json().then(err => { throw err; });
            // li要素にidを付与
            const li = document.getElementById(`todo-${id}`);
            // li要素を削除
            if (li) li.remove();
        })
        // エラーがある場合、アラートで表示
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 完了切替
// ----------------------------
function toggleTodo(id) {
    fetch(`/todos/${id}/toggle`, { method: 'PATCH' })
    	// APIから返ってきたJSON文字列をJavascriptオブジェクトに変換
        .then(res => res.json())
        .then(updated => updateTodoUI(updated))
        // エラーがある場合、アラートで表示
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 初期表示
// ----------------------------
fetchTodos();
