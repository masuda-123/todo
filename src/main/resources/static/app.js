// ============================
// 要素取得
// ============================
const todoList = document.getElementById('todo-list');
const inputWrapper = document.querySelector(".input-wrapper");
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
const editCloseBtn = document.querySelector(".edit-close-btn");
const editModal = document.getElementById("edit-modal");
const editInput = document.getElementById("edit-input");
const editMemo = document.getElementById("edit-memo");
const listModal = document.getElementById("list-modal");
const listNameInput = document.getElementById("list-name-input");
const listColorInput = document.getElementById("list-color-input");
const saveListBtn = document.getElementById("save-list-btn");
const listCloseBtn = document.querySelector(".list-close-btn");
const emptyMessage = document.getElementById("empty-message");
let sortable = null;
let currentListId = null;

const savedListId = localStorage.getItem("currentListId");
currentListId = savedListId ? Number(savedListId) : null;


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
	if (!currentListId) return;
    fetch(`/todos?listId=${currentListId}`)
        .then(res => res.json())
        .then(data => {
            todoList.innerHTML = "";

            data.forEach(todo => {
                const li = document.createElement("li");
                li.id = `todo-${todo.id}`;
                li.style.setProperty("--list-color", todo.listColor);
                const checkbox = createCheckbox(todo);

                const text = document.createElement("div");
                text.classList.add("text-wrapper");
                const title = document.createElement("div");
				title.classList.add("todo-title");
				title.textContent = todo.title;
				
				title.style.textDecoration =
				    todo.done ? "line-through" : "none";
				
				text.appendChild(title);
				
				if (todo.memo) {
				    const memo = document.createElement("div");
				    memo.classList.add("todo-memo");
				    memo.textContent = todo.memo;
				    memo.style.textDecoration = todo.done ? "line-through" : "none";
				
				    text.appendChild(memo);
				}

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
            initSortable();
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
		editTodo(todo);
    });

    return btn;
}

// ============================
// 追加
// ============================
addBtn.addEventListener("click", () => {
    const title = inputTitle.value.trim();
    if (!title) return;

    if (currentListId == null) {
        alert("リストを選択してください");
        return;
    }

    fetch("/todos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            title,
            listId: currentListId
        })
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
    editMemo.value = todo.memo;
    editModal.classList.remove("hidden");
}


// ============================
// 編集モーダル閉じる
// ============================
editCloseBtn?.addEventListener("click", () => {
    editModal.classList.add("hidden");
});

// ============================
// タスク名の編集の保存
// ============================
saveBtn?.addEventListener("click", () => {
    const title = editInput.value.trim();
    const memo = editMemo.value.trim();
    if (!title) return;

    fetch(`/todos/${currentTodo.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, memo})
    })
    .then(res => res.json())
    .then(updated => {
        fetchTodos();
        editModal.classList.add("hidden");
    });
});

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
    inputWrapper.classList.add("fade-hidden");

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
    inputWrapper.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerSwitchBtn?.addEventListener("click", () => {
    selectedIds.forEach(toggleTodo);
    selectedIds.clear();
    bulkMode = null;
    inputWrapper.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerCancelBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        bulkMode = null;
        selectedIds.clear();
        inputWrapper.classList.remove("fade-hidden");
        fetchTodos();
        updateFooter();
    });
});

// ============================
// リストがない場合にUIを更新
// ============================
function updateUI(hasLists) {

    if (hasLists) {
        inputWrapper.style.display = "flex";
        menuBtn.style.display = "block";
        emptyMessage.classList.add("hidden");
    } else {
        inputWrapper.style.display = "none";
        menuBtn.style.display = "none";
        emptyMessage.classList.remove("hidden");
    }
}

// ============================
// 並び替え
// ============================
function initSortable() {

    if (bulkMode) {

        if (sortable) {
            sortable.destroy();
            sortable = null;
        }

        return;
    }

    if (sortable) {
        sortable.destroy();
    }

    sortable = new Sortable(todoList, {
        animation: 150,

        ghostClass: "sortable-ghost",
        chosenClass: "sortable-chosen",
        onEnd: () => {
		    const orderedIds = [...todoList.children].map(li =>
		        Number(li.id.replace("todo-", ""))
		    );
		
		    fetch("/todos/reorder", {
		        method: "PATCH",
		        headers: {
		            "Content-Type": "application/json"
		        },
		        body: JSON.stringify(orderedIds)
		    });
		}
    });
}

// ============================
// リストの取得
// ============================
function fetchLists() {

    fetch("/lists")
        .then(res => res.json())
        .then(data => {
			
			updateUI(data.length > 0);

            const sidebar =
                document.getElementById("list-sidebar");

            sidebar.innerHTML = "";

            data.forEach(list => {
				const li = document.createElement("li");

                li.innerHTML = `
				    <span style="display:inline-block;
				                 width:10px;
				                 height:10px;
				                 background:${list.color};
				                 border-radius:50%;
				                 margin-right:6px;"></span>
				    ${list.name}
				`;
				
				sidebar.appendChild(li);

				if (Number(list.id) === currentListId){
				
				    li.classList.add("active");
				
				    document.getElementById("current-list-title").textContent = list.name;
				
				    document.getElementById("current-list-title").style.color = list.color;
				    
				    fetchTodos();
				}
                
                li.addEventListener("click", () => {
			
			    	currentListId = Number(list.id);
			    	
			    	localStorage.setItem("currentListId", currentListId);
			
			    	document.getElementById("current-list-title").textContent = list.name;
			    	document.getElementById("current-list-title").style.color = list.color;
			    	document.querySelectorAll("#list-sidebar li").forEach(item => item.classList.remove("active"));
			    	li.classList.add("active");
			    	fetchTodos();
			    });
        	});
        	if (currentListId == null && data.length > 0) {
				currentListId = Number(data[0].id);
				localStorage.setItem("currentListId", currentListId);
				document.getElementById("current-list-title").textContent = data[0].name;
				fetchTodos();
			}
        });
}


// ============================
// リスト作成モーダルを開く
// ============================
document.getElementById("add-list-btn").addEventListener("click", () => {
    listModal.classList.remove("hidden");
});

// ============================
// リスト作成モーダルを閉じる
// ============================
listCloseBtn?.addEventListener("click", () => {
    listModal.classList.add("hidden");
});


// ============================
// リストの追加
// ============================
saveListBtn.addEventListener("click", () => {

    const name = listNameInput.value.trim();
    const color = listColorInput.value;

    if (!name) return;

    fetch("/lists", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ name, color })
    }).then(() => {
        listNameInput.value = "";
        listModal.classList.add("hidden");
        fetchLists();
    });
});



// ============================
// 初期表示
// ============================
fetchLists();
