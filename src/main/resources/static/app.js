// ============================
// 要素取得
// ============================
const todoList = document.getElementById('todo-list');
const inputWraperr = document.querySelector(".input-wrapper");
const addBtn = document.getElementById('add-btn');
const inputTitle = document.getElementById('input-title');

const menuBtn = document.getElementById("menu-btn");
const actionMenu = document.getElementById("action-menu");


const deleteFooter = document.getElementById("selection-footer-delete-mode");
const switchFooter = document.getElementById("selection-footer-switch-status-mode");

const footerDeleteBtn = document.getElementById("footer-delete");
const footerSwitchBtn = document.getElementById("footer-switch-status");

const footerCancelBtns = document.querySelectorAll("#footer-cancel");

const saveBtn = document.getElementById("save-btn");
const closeBtn = document.querySelector(".close-btn");
const editModal = document.getElementById("edit-modal");
const editInput = document.getElementById("edit-input");

// ============================
// モード管理用変数
// ============================
let bulkMode = null; 
const selectedIds = new Set();
let currentTodo = null;

// ============================
// 一覧取得
// ============================
function fetchTodos() {
    fetch("/todos")
        .then(res => res.json())
        .then(data => {
            todoList.innerHTML = "";

            data.forEach(todo => {
                const li = document.createElement("li");
                li.id = `todo-${todo.id}`;

                const checkbox = createCheckbox(todo);

                const text = document.createElement("div");
                text.classList.add("text-wrapper");
                text.textContent = todo.title;
                text.style.textDecoration = todo.done ? "line-through" : "none";

                const btnWrapper = document.createElement("div");
                btnWrapper.classList.add("btn-wrapper");

                if (!bulkMode) {
                    btnWrapper.appendChild(createEditBtn(todo, text));
                }

                if (bulkMode) {
                    li.appendChild(text);
                    li.appendChild(checkbox);
                } else {
                    li.appendChild(checkbox);
                    li.appendChild(text);
                    li.appendChild(btnWrapper);
                }

                todoList.appendChild(li);
            });
        })
        .catch(err => console.error(err));
}

// ============================
// チェックボックス
// ============================
function createCheckbox(todo) {
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.classList.add("todo-checkbox");
	if (bulkMode) {
	    checkbox.classList.add("bulk");
	} else {
	    checkbox.classList.remove("bulk");
	}

    const isBulk = bulkMode !== null;

    if (isBulk) {
        checkbox.checked = selectedIds.has(todo.id);

        checkbox.addEventListener("click", (e) => {
            e.stopPropagation();

            if (checkbox.checked) {
                selectedIds.add(todo.id);
            } else {
                selectedIds.delete(todo.id);
            }
            updateFooter();
        });

    } else {
        checkbox.checked = todo.done;

        checkbox.addEventListener("click", (e) => {
            e.stopPropagation();
            toggleTodo(todo.id);
        });
    }

    return checkbox;
}

// ============================
// 各タスクの編集ボタン
// ============================

function createEditBtn(todo, textWrapper) {
    const btn = document.createElement("button");
    btn.innerHTML = '<i class="fa-solid fa-pen"></i>';
    btn.classList.add("icon-btn", "edit-btn");

    btn.addEventListener("click", (e) => {
        e.stopPropagation();
        editTodo({
            id: todo.id,
            title: textWrapper.textContent
        });
    });

    return btn;
}

// ============================
// 追加
// ============================
addBtn.addEventListener("click", () => {
    const title = inputTitle.value.trim();
    if (!title) return;

    fetch("/todos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title })
    }).then(() => {
        inputTitle.value = "";
        fetchTodos();
    });
});


// ============================
// 削除
// ============================
function deleteTodo(id) {
    fetch(`/todos/${id}`, { method: "DELETE" })
        .then(() => fetchTodos());
}

// ============================
// 編集モーダルを開く
// ============================
function editTodo(todo) {
    currentTodo = todo;
    editInput.value = todo.title;
    editModal.classList.remove("hidden");
}


// ============================
// 編集モーダル閉じる
// ============================
closeBtn?.addEventListener("click", () => {
    editModal.classList.add("hidden");
});

// ============================
// タスク名の編集の保存
// ============================
saveBtn?.addEventListener("click", () => {
    const title = editInput.value.trim();
    if (!title) return;

    fetch(`/todos/${currentTodo.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title })
    })
    .then(res => res.json())
    .then(updated => {
        updateTodoUI(updated);
        editModal.classList.add("hidden");
    });
});

// ============================
// タスク名変更後のUI部分更新
// ============================
function updateTodoUI(todo) {
    const li = document.getElementById(`todo-${todo.id}`);
    if (!li) return;

    const text = li.querySelector(".text-wrapper");
    text.textContent = todo.title;
    text.style.textDecoration = todo.done ? "line-through" : "none";
}

// ============================
// ステータス切替
// ============================
function toggleTodo(id) {
    fetch(`/todos/${id}/toggle`, { method: "PATCH" })
        .then(() => fetchTodos());
}

// ============================
// メニュー
// ============================
menuBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    actionMenu.classList.toggle("hidden");
});

document.addEventListener("click", () => {
    actionMenu?.classList.add("hidden");
});

// ============================
// 一括モード開始
// ============================
document.getElementById("bulk-delete")?.addEventListener("click", () => {
    enterBulkMode("delete");
});

document.getElementById("bulk-switch-status")?.addEventListener("click", () => {
    enterBulkMode("switch");
});
function enterBulkMode(mode) {
    bulkMode = mode;
    selectedIds.clear();

    actionMenu?.classList.add("hidden");
    inputWraperr.classList.add("fade-hidden");

    fetchTodos();
    updateFooter();
}

// ============================
// フッター更新
// ============================
function updateFooter() {
    const hasSelection = selectedIds.size > 0;

    if (!bulkMode) {
        deleteFooter?.classList.add("hidden");
        switchFooter?.classList.add("hidden");
        return;
    }

    if (bulkMode === "delete") {
        deleteFooter?.classList.remove("hidden");
        footerDeleteBtn.style.display = hasSelection ? "inline-block" : "none";
    }

    if (bulkMode === "switch") {
        switchFooter?.classList.remove("hidden");
        footerSwitchBtn.style.display = hasSelection ? "inline-block" : "none";
    }
}

// ============================
// フッター操作
// ============================
footerDeleteBtn?.addEventListener("click", () => {
    selectedIds.forEach(deleteTodo);
    selectedIds.clear();
    bulkMode = null;
    inputWraperr.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerSwitchBtn?.addEventListener("click", () => {
    selectedIds.forEach(toggleTodo);
    selectedIds.clear();
    bulkMode = null;
    inputWraperr.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerCancelBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        bulkMode = null;
        selectedIds.clear();
        inputWraperr.classList.remove("fade-hidden");
        fetchTodos();
        updateFooter();
    });
});

// ============================
// 初期表示
// ============================
fetchTodos();
