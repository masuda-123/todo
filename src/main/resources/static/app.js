// ============================
// 要素取得
// ============================
const todoList = document.getElementById('todo-list');

const menuBtn = document.getElementById("menu-btn");
const actionMenu = document.getElementById("action-menu");


const deleteFooter = document.getElementById("selection-footer-delete-mode");
const doneFooter = document.getElementById("selection-footer-done-mode");

const footerDeleteBtn = document.getElementById("footer-delete");
const footerDoneBtn = document.getElementById("footer-done");
const footerNotDoneBtn = document.getElementById("footer-not-done");
const footerCancelBtns = document.querySelectorAll("#footer-cancel");

const openAddTodoMoalBtn = document.getElementById("open-add-todo-modal-btn");
const addTodoCloseBtn = document.getElementById("add-todo-close-modal-btn");
const addTodoModal = document.getElementById("add-todo-modal");
const addTodoInput = document.getElementById("add-todo-input");
const addTodoMemo = document.getElementById("add-todo-memo");
const addTodoDateTime = document.getElementById("add-todo-datetime");
const addTodoBtn = document.getElementById("add-todo-btn");

const editTodoCloseBtn = document.getElementById("edit-todo-close-modal-btn");
const editModal = document.getElementById("edit-modal");
const editInput = document.getElementById("edit-input");
const editMemo = document.getElementById("edit-memo");
const editDateTime = document.getElementById("edit-datetime");
const editBtn = document.getElementById("edit-btn");

const listModal = document.getElementById("list-modal");
const listNameInput = document.getElementById("list-name-input");
const listColorInput = document.getElementById("list-color-input");
const addListBtn = document.getElementById("add-list-btn");
const addlistCloseBtn = document.getElementById("add-list-close-modal-btn");

const emptyMessage = document.getElementById("empty-message");
const listContextMenu = document.getElementById("list-context-menu");

const editListMenuBtn = document.getElementById("edit-list-menu-btn");
const deleteListMenuBtn = document.getElementById("delete-list-menu-btn");

const editListModal = document.getElementById("edit-list-modal");
const editListNameInput = document.getElementById("edit-list-name-input");
const editListColorInput = document.getElementById("edit-list-color-input");
const editListBtn = document.getElementById("edit-list-btn");
const editListCloseBtn = document.getElementById("edit-list-close-modal-btn");

let currentTodo = null;
let sortable = null;
let contextTargetListId = null;
let editingList = null;
let deletingList = null;
let hasLists = false;
let selectedLists = false;
let hasTasks = false;

const savedListId = localStorage.getItem("currentListId");
currentListId = savedListId ? Number(savedListId) : null;


// ============================
// モード管理用変数
// ============================
let bulkMode = false;
let deleteMode = false; 
let doneMode = false;
const selectedTaskIds = new Set();

// ============================
// タスクの一覧取得
// ============================
function fetchTodos() {
	if (!currentListId) return;
    fetch(`/lists/${currentListId}/todos`)
        .then(res => res.json())
        .then(data => {
			todoCache = data;
			hasTasks = data.length > 0;
			updateHeader();
            todoList.innerHTML = "";
            data.forEach(todo => {
                const li = document.createElement("li");
                li.id = `todo-${todo.id}`;
                li.style.setProperty("--list-color", todo.listColor);
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
                
                if (deleteMode){
					li.appendChild(text);
                    li.appendChild(createCheckbox(todo));
				} else if(doneMode) {
					li.appendChild(text);
				} else {
					li.appendChild(createCheckbox(todo));
                    li.appendChild(text);
                   	const btnWrapper = document.createElement("div");
                	btnWrapper.classList.add("btn-wrapper");
                    li.appendChild(btnWrapper);
                    btnWrapper.appendChild(createEditBtn(todo, text));
				}

                todoList.appendChild(li);
            });
            initSortable();
        })
        .catch(err => console.error(err));
}

// ============================
// タスクのチェックボックス
// ============================
function createCheckbox(todo) {
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.classList.add("todo-checkbox");
	if (deleteMode) {
	    checkbox.classList.add("bulk");
	} else {
	    checkbox?.classList.remove("bulk");
	}

    if (deleteMode) {
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
// タスク作成モーダルを開く
// ============================
openAddTodoMoalBtn.addEventListener("click", () => {
	addTodoInput.value = "";
    addTodoMemo.value = "";
    addTodoDateTime.value = "";
    addTodoModal.classList.remove("hidden");
});

// ============================
// タスク作成モーダル閉じる
// ============================
addTodoCloseBtn?.addEventListener("click", () => {
    addTodoModal.classList.add("hidden");
});

// ============================
// タスクの作成
// ============================
addTodoBtn?.addEventListener("click", () => {
    const title = addTodoInput.value.trim();
    const memo = addTodoMemo.value.trim();
    const dateTime = addTodoDateTime.value.trim();
    if (!title) return;

    fetch(`/todos`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, memo, dateTime, listId: currentListId})
    })
    .then(res => res.json())
    .then(() => {
        fetchTodos();
        addTodoModal.classList.add("hidden");
    });
});

// ============================
// タスクの作成モーダルの入力内容をリセット
// ============================
function resetAddTodoForm() {
    addTodoInput.value = "";
    addTodoMemo.value = "";
    addTodoDateTime.value = "";
}


// ============================
// タスクの削除
// ============================
function deleteTodo(id) {
    fetch(`/todos/${id}`, { method: "DELETE" })
        .then(() => fetchTodos());
}

// ============================
// タスクの編集モーダルを開く
// ============================
function editTodo(todo) {
    currentTodo = todo;
    editInput.value = todo.title;
    editMemo.value = todo.memo;
    editDateTime.value = todo.dateTime ? todo.dateTime.slice(0, 16): "";
    editModal.classList.remove("hidden");
}


// ============================
// タスクの編集モーダル閉じる
// ============================
editTodoCloseBtn?.addEventListener("click", () => {
    editModal.classList.add("hidden");
});

// ============================
// タスクの編集の保存
// ===========================
editBtn?.addEventListener("click", () => {
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
    .then(() => {
        fetchTodos();
        editModal.classList.add("hidden");
    });
});

// ============================
// タスクのステータス切替
// ============================
function toggleTodo(id) {
    fetch(`/todos/${id}/toggle`, { method: "PATCH" })
        .then(() => fetchTodos());
}

// ============================
// 全てのタスクのステータス切替
// ============================
function updateAllTodoStatus(done) {
    fetch(`/lists/${currentListId}/todos?done=${done}`, { method: "PATCH" })
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
	bulkMode = true;
	deleteMode = true;
    selectedTaskIds.clear();
    actionMenu?.classList.add("hidden");

    fetchTodos();
    updateFooter();
});

document.getElementById("bulk-done")?.addEventListener("click", () => {
	bulkMode = true;
	doneMode = true;
    actionMenu?.classList.add("hidden");
    
    fetchTodos();
    updateFooter();
});

// ============================
// ヘッダーの更新
// ============================
function updateHeader() {
	console.log(selectedLists);
    if (selectedLists) {
		emptyMessage.classList.add("hidden");
		if(bulkMode){
			menuBtn.style.display = "none";
			openAddTodoMoalBtn.classList.add("hidden");
		}else if(hasTasks){
			menuBtn.style.display = "block";
			openAddTodoMoalBtn.classList.remove("hidden");
		} else {
	        menuBtn.style.display = "none";
	        openAddTodoMoalBtn.classList.remove("hidden");
	    }
    } else {
		openAddTodoMoalBtn.classList.add("hidden");
        menuBtn.style.display = "none";
        emptyMessage.classList.remove("hidden");
        if(hasLists){
        	emptyMessage.textContent = "リストを選択してください";
        } else {
        	emptyMessage.textContent = "リストを作成してください";
       	}
	}
}

// ============================
// フッター更新
// ============================
function updateFooter() {
	
	if (deleteMode) {
        deleteFooter?.classList.remove("hidden");
    } else if(doneMode) {
		doneFooter?.classList.remove("hidden");
	} else {
		deleteFooter?.classList.add("hidden");
		doneFooter?.classList.add("hidden");
	}
}

// ============================
// フッター操作
// ============================
footerDeleteBtn?.addEventListener("click", () => {
    selectedTaskIds.forEach(deleteTodo);
    selectedTaskIds.clear();
    bulkMode = false;
    deleteMode = false;
    fetchTodos();
    updateHeader();
    updateFooter();
});

footerDoneBtn?.addEventListener("click", () => {
	updateAllTodoStatus(true);
    bulkMode = false;
    doneMode = false;
    openAddTodoMoalBtn.classList.remove("hidden");
    fetchTodos();
    updateHeader();
    updateFooter();
});

footerNotDoneBtn?.addEventListener("click", () => {
	updateAllTodoStatus(false);
    bulkMode = false;
    doneMode = false;
    openAddTodoMoalBtn.classList.remove("hidden");
    fetchTodos();
    updateFooter();
});

footerCancelBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        bulkMode = false;
        if(deleteMode) {
        	deleteMode = false;
        	selectedTaskIds.clear();
        }else{
			doneMode = false;
		}
        openAddTodoMoalBtn.classList.remove("hidden");
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
			updateHeader();
			
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
					hasLists = true;
			    	selectedLists = true;
			    	currentListId = Number(list.id);
			    	
			    	localStorage.setItem("currentListId", currentListId);
			
			    	document.getElementById("current-list-title").textContent = list.name;
			    	document.getElementById("current-list-title").style.color = list.color;
			    	document.querySelectorAll("#list-sidebar li").forEach(item => item.classList.remove("active"));
			    	li.classList.add("active");
			    	fetchTodos();
			    });
        	});
        });
}

// ============================
// リスト作成モーダルを開く
// ============================
document.getElementById("open-add-list-modal-btn").addEventListener("click", () => {
	listNameInput.value = "";
    listColorInput.value = "#4f46e5";
    listModal.classList.remove("hidden");
});

// ============================
// リスト作成モーダルを閉じる
// ============================
addlistCloseBtn?.addEventListener("click", () => {
    listModal.classList.add("hidden");
});


// ============================
// リストの作成
// ============================
addListBtn.addEventListener("click", () => {

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

editListBtn.addEventListener("click", async () => {

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
	    ).textContent = "Todo タスク";
	    
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
// タスクの通知を出す
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
