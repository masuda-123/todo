// ----------------------------
// HTML要素
// ----------------------------
const todoList = document.getElementById('todo-list');
const addBtn = document.getElementById('add-btn');
const inputTitle = document.getElementById('input-title');
const saveBtn = document.getElementById("save-btn")
const closeBtn = document.querySelector(".close-btn")
let currentTodo = null;

// ----------------------------
// チェックボックス作成
// ----------------------------
function createToggleCheckbox(todo) {
    const checkbox = document.createElement('input');

    checkbox.type = 'checkbox';
    checkbox.checked = todo.done;

    checkbox.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleTodo(todo.id);
    });

    return checkbox;
}

// ----------------------------
// 削除ボタン作成
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

function createEditBtn(todo, textWrapper) {
    const btn = document.createElement('button');
    btn.innerHTML = '<i class="fa-solid fa-pen"></i>';
    btn.classList.add('icon-btn', 'edit-btn');
    btn.addEventListener('click', (e) => {
        e.stopPropagation(); // ← これ重要（親のクリック防止）
        editTodo({
            id: todo.id,
            title: textWrapper.textContent
        });
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
// タスクの取得＆タスク一覧の表示
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
				btnWrapper.appendChild(createEditBtn(todo, textWrapper));
                btnWrapper.appendChild(createDeleteBtn(todo));
                todoList.appendChild(li);
            });
        })
        .catch(err => alert(JSON.stringify(err)));
}

// ----------------------------
// バリデーション
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
// タスクの追加
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
// 編集画面を開く
// ----------------------------
function editTodo({id, title}) {
    const modal = document.getElementById("edit-modal");
    const input = document.getElementById("edit-input");
    currentTodo = { id, title };
    input.value = currentTodo.title;
    modal.classList.remove("hidden");
}

// ----------------------------
// 編集内容を保存
// ----------------------------
saveBtn.addEventListener("click", () => {
    const input = document.getElementById("edit-input");
    const newTitle = validateTitle(input.value);
    if (!newTitle) return;

    fetch(`/todos/${currentTodo.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: newTitle })
    })
    .then(res => {
        if (!res.ok) return res.json().then(err => { throw err; });
        return res.json();
    })
    .then(updated => {
        updateTodoUI(updated);
        closeModal();
    })
    .catch(err => alert(JSON.stringify(err)));
});

// ----------------------------
// 編集画面を閉じる
// ----------------------------
closeBtn.addEventListener("click", closeModal);
function closeModal() {
    document.getElementById("edit-modal").classList.add("hidden");
}

// ----------------------------
// タスクの削除
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
// タスクの完了切替
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
