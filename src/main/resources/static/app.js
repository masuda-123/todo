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
const editDateTime = document.getElementById("edit-datetime");
const listModal = document.getElementById("list-modal");
const listNameInput = document.getElementById("list-name-input");
const listColorInput = document.getElementById("list-color-input");
const saveListBtn = document.getElementById("save-list-btn");
const listCloseBtn = document.querySelector(".list-close-btn");
const emptyMessage = document.getElementById("empty-message");
const listContextMenu = document.getElementById("list-context-menu");
const editListMenuBtn = document.getElementById("edit-list-menu-btn");
const deleteListMenuBtn = document.getElementById("delete-list-menu-btn");
const editListModal = document.getElementById("edit-list-modal");
const editListNameInput = document.getElementById("edit-list-name-input");
const editListColorInput = document.getElementById("edit-list-color-input");
const updateListBtn = document.getElementById("update-list-btn");
const editListCloseBtn = document.querySelector(".edit-list-close-btn");

let contextTargetListId = null;
let sortable = null;
let currentListId = null;
let editingList = null;
let deletingList = null;
let hasLists = false;
let selectedLists = false;
let hastasks = false

const savedListId = localStorage.getItem("currentListId");
currentListId = savedListId ? Number(savedListId) : null;


// ============================
// モード管理用変数
// ============================
let bulkMode = null; 
const selectedTaskIds = new Set();
let currentTodo = null;

// ============================
// 一覧取得
// ============================
function fetchTodos() {
	if (!currentListId) return;
    fetch(`/lists/${currentListId}/todos`)
        .then(res => res.json())
        .then(data => {
			todoCache = data;
			console.log(data);
			updateUI(hasLists, selectedLists, data.length > 0);
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
				
				if (todo.dateTime){
				    const dateTime = document.createElement("div");
				    dateTime.classList.add("todo-datetime");
				
				    const d = new Date(todo.dateTime);
				
				    dateTime.textContent =
				        d.toLocaleString("ja-JP", {
				            year: "numeric",
				            month: "2-digit",
				            day: "2-digit",
				            hour: "2-digit",
				            minute: "2-digit"
				        });
				
					dateTime.style.textDecoration = todo.done ? "line-through" : "none";
				    text.appendChild(dateTime);
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
        checkbox.checked = selectedTaskIds.has(todo.id);

        checkbox.addEventListener("click", (e) => {
            e.stopPropagation();

            if (checkbox.checked) {
                selectedTaskIds.add(todo.id);
            } else {
                selectedTaskIds.delete(todo.id);
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
    editDateTime.value = todo.dateTime ? todo.dateTime.slice(0, 16): "";
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
    const dateTime = editDateTime.value.trim();
    if (!title) return;

    fetch(`/todos/${currentTodo.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, memo, dateTime})
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
    selectedTaskIds.clear();

    actionMenu?.classList.add("hidden");
    inputWrapper.classList.add("fade-hidden");

    fetchTodos();
    updateFooter();
}

// ============================
// フッター更新
// ============================
function updateFooter() {
    const hasSelection = selectedTaskIds.size > 0;

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
    selectedTaskIds.forEach(deleteTodo);
    selectedTaskIds.clear();
    bulkMode = null;
    inputWrapper.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerSwitchBtn?.addEventListener("click", () => {
    selectedTaskIds.forEach(toggleTodo);
    selectedTaskIds.clear();
    bulkMode = null;
    inputWrapper.classList.remove("fade-hidden");
    fetchTodos();
    updateFooter();
});

footerCancelBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        bulkMode = null;
        selectedTaskIds.clear();
        inputWrapper.classList.remove("fade-hidden");
        fetchTodos();
        updateFooter();
    });
});

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
			hasLists = data.length > 0;
			updateUI(hasLists, selectedLists, hastasks)
			
			selectedLists = (currentListId != null);

            const sidebar =
                document.getElementById("list-sidebar");

            sidebar.innerHTML = "";

            data.forEach(list => {
				const li = document.createElement("li");
				
				li.addEventListener("contextmenu", (e) => {
				    e.preventDefault();
				    e.stopPropagation();
				
				    contextTargetListId = list.id;
				
				    listContextMenu.style.left = `${e.pageX}px`;
				    listContextMenu.style.top = `${e.pageY}px`;
				
				    listContextMenu.classList.remove("hidden");
				});
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
			    	hasLists = true;
			    	selectedLists = true;
			    	fetchTodos();
			    });
        	});
        });
}

// ============================
// リストやタスクがない場合、リストが選択されていない場合にUIを更新
// ============================
function updateUI(hasLists, selectedLists, hasTasks) {

    if (!selectedLists) {
		inputWrapper.style.display = "none";
        menuBtn.style.display = "none";
        if(!hasLists){
        	emptyMessage.classList.remove("hidden");
        	emptyMessage.textContent = "リストを作成してください";
        } else {
        	emptyMessage.classList.remove("hidden");
        	emptyMessage.textContent = "リストを選択してください";
       	}
    } else {
		if(!hasTasks){
			menuBtn.style.display = "none";
		} else {
	        menuBtn.style.display = "block";
	    }
	     inputWrapper.style.display = "flex";
	     emptyMessage.classList.add("hidden");
	}
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
    }).then(res => res.json())
		.then(createdList => {
	
	    currentListId = createdList.id;
	
	    localStorage.setItem(
	        "currentListId",
	        currentListId
	    );
	
	    listNameInput.value = "";
	
	    listModal.classList.add("hidden");
	
	    fetchLists();
	});
});

function closeListContextMenu() {
    listContextMenu.classList.add("hidden");
}

// ============================
// リストの編集
// ============================
editListMenuBtn.addEventListener("click", async () => {

    const res = await fetch("/lists");

    const lists = await res.json();

    const targetList = lists.find(
        list => list.id === contextTargetListId
    );

    if (!targetList) return;

    editingList = targetList;

    editListNameInput.value = targetList.name;

    editListColorInput.value = targetList.color;

    editListModal.classList.remove("hidden");

    closeListContextMenu();
});

editListCloseBtn.addEventListener("click", () => {
    editListModal.classList.add("hidden");
});

updateListBtn.addEventListener("click", async () => {

    const name = editListNameInput.value.trim();

    const color = editListColorInput.value;

    if (!name) return;

    await fetch(`/lists/${editingList.id}`, {
        method: "PATCH",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            name,
            color
        })
    });

    editListModal.classList.add("hidden");

    fetchLists();
});


// ============================
// リストの削除
// ============================
deleteListMenuBtn.addEventListener("click", async () => {

    const confirmed = confirm(
        "このリストを削除しますか？"
    );

    if (!confirmed) return;

    await fetch(`/lists/${contextTargetListId}`, {
        method: "DELETE"
    });

	if (currentListId === contextTargetListId) {
	
	    currentListId = null;
	
	    localStorage.removeItem("currentListId");
	    selectedLists = false;
	
	    todoList.innerHTML = "";
	
	    document.getElementById(
	        "current-list-title"
	    ).textContent = "Todo リスト";
	    
	    document.getElementById("current-list-title").style.color = "#000000";
	}

    closeListContextMenu();

    fetchLists();
});

// ============================
// リストのメニュー閉じる
// ============================
document.addEventListener("click", (e) => {

    if (listContextMenu.classList.contains("hidden")) {
        return;
    }

    if (listContextMenu.contains(e.target)) {
        return;
    }

    closeListContextMenu();
});

listContextMenu.addEventListener("click", (e) => {
    e.stopPropagation();
});


// ============================
// 通知を出す
// ============================
async function checkNotifications() {
    if (Notification.permission !== "granted") return;

    const res = await fetch(`/todos`);
    const todos = await res.json();
    const now = new Date();

    todos.forEach(todo => {
        if (!todo.dateTime) return;
        if (todo.done) return;
        if (todo.notified === true) return;

        const due = new Date(todo.dateTime);

        if (due <= now) {
            showNotification(todo);
        }
    });
}

function showNotification(todo) {
    new Notification("タスクの時間です", {
        body: todo.title,
    });
    
    fetch(`/todos/${todo.id}/notified`, {
        method: "PATCH"
    });
}

// ============================
// 初期表示
// ============================
fetchLists();
Notification.requestPermission();
setInterval(checkNotifications, 30000);
