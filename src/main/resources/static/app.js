// ----------------------------
// HTML要素
// ----------------------------
const todoList = document.getElementById('todo-list');
const addBtn = document.getElementById('add-btn');
const inputTitle = document.getElementById('input-title');

// ----------------------------
// チェックボックス作成関数
// ----------------------------
function createToggleCheckbox(todo) {
    const checkbox = document.createElement('input');

    checkbox.type = 'checkbox';
    checkbox.checked = todo.done; // ← 今の状態を反映

    checkbox.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleTodo(todo.id);
    });

    return checkbox;
}

// ----------------------------
// 削除ボタン作成関数
// ----------------------------
function createDeleteBtn(todo) {
    const btn = document.createElement('button');
    btn.innerHTML = '<i class="fa-solid fa-trash"></i>';
    btn.classList.add('icon-btn', 'delete-btn');
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
    const li = document.getElementById(`todo-${updatedTodo.id}`);
    if (!li) return;
    const textDiv = li.querySelector('.text-wrapper');
    textDiv.textContent = updatedTodo.title;
    textDiv.style.textDecoration = updatedTodo.done ? 'line-through' : 'none';
}


// ----------------------------
// Todo取得＆描画
// ----------------------------
function fetchTodos() {
    fetch('/todos')
        .then(res => res.json())
        .then(data => {
            todoList.innerHTML = '';
            data.forEach(todo => {
                const li = document.createElement('li');
                li.id = `todo-${todo.id}`;
                
                const checkbox = createToggleCheckbox(todo);
                li.appendChild(checkbox);

                const textWrapper = document.createElement('div');
                li.appendChild(textWrapper);
				textWrapper.classList.add('text-wrapper');
				textWrapper.textContent = todo.title;  
                // 完了なら取り消し線を表示
               	textWrapper.style.textDecoration = todo.done ? "line-through" : "none";
               	
                const btnWrapper = document.createElement('div');
                btnWrapper.classList.add('btn-wrapper')
                li.appendChild(btnWrapper);
                btnWrapper.appendChild(createDeleteBtn(todo));

                textWrapper.addEventListener('click', () => editTodo(todo));
                todoList.appendChild(li);
            });
        })
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// バリデーション関数
// ----------------------------
function validateTitle(input) {
	if (input === null) return null;
    const t = input?.trim();
    if (!t) {
        alert('タスク名を入力してください');
        return null;
    }
    if (t.length > 50) {
        alert('タスク名は50文字以内で入力してください');
        return null;
    }
    return t;
}

// ----------------------------
// 追加
// ----------------------------
addBtn.addEventListener('click', () => {
    const title = validateTitle(inputTitle.value);
    if (!title) return;
    fetch('/todos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title })
    })
    .then(res => {
        if (!res.ok) return res.json().then(err => { throw err; });
        return res.json();
    })
    .then(() => {
        inputTitle.value = '';
        fetchTodos();
    })
    .catch(err => alert(JSON.stringify(err)));
});

// ----------------------------
// 編集関数
// ----------------------------
function editTodo(todo) {
	const newTitle = validateTitle(prompt("タスク名の編集", todo.title));
    if (!newTitle) return;
    fetch(`/todos/${todo.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: newTitle })
    })
    .then(res => {
        if (!res.ok) return res.json().then(err => { throw err; });
        return res.json();
    })
    .then(updated => updateTodoUI(updated))
    .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 削除
// ----------------------------
function deleteTodo(id) {
    fetch(`/todos/${id}`, { method: 'DELETE' })
        .then(res => {
            if (!res.ok) return res.json().then(err => { throw err; });
            const li = document.getElementById(`todo-${id}`);
            if (li) li.remove();
        })
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 完了切替
// ----------------------------
function toggleTodo(id) {
    fetch(`/todos/${id}/toggle`, { method: 'PATCH' })
        .then(res => res.json())
        .then(updated => updateTodoUI(updated))
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// 初期表示
// ----------------------------
fetchTodos();
