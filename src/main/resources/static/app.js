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

const openAddTodoModalBtn = document.getElementById("open-add-todo-modal-btn");
const addTodoCloseBtn = document.getElementById("add-todo-close-modal-btn");
const addTodoModal = document.getElementById("add-todo-modal");
const addTodoInput = document.getElementById("add-todo-input");
const addTodoMemo = document.getElementById("add-todo-memo");
const addTodoDateTime = document.getElementById("add-todo-datetime");
const addTodoNotify = document.getElementById("add-todo-notify");
const addTodoBtn = document.getElementById("add-todo-btn");

const detailDeleteTodoBtn = document.getElementById("detail-delete-todo-btn");
const detailTodoTitle = document.getElementById("detail-title");
const detailTodoMemo = document.getElementById("detail-memo");
const detailTodoDateTime = document.getElementById("detail-datetime");
const detailTodoNotify = document.getElementById("detail-notify");

const allFilter = document.getElementById("all-filter");
const scheduledFilter = document.getElementById("scheduled-filter");
const todayFilter = document.getElementById("today-filter");
const tomorrowFilter = document.getElementById("tomorrow-filter");
const sidebarMylist = document.getElementById("sidebar-mylist");
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

let todoCache = [];
let isSaving = false;
let selectedTodoId = null;
let todoSortable = null;
let listSortable = null;
let contextTargetListId = null;
let editingList = null;
let deletingList = null;
let hasTasks = false;

let currentFilter = "all";
let currentListId = null;

const savedListId = localStorage.getItem("currentListId");

if (savedListId) {
    currentListId = Number(savedListId);
    currentFilter = null;
}

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
    let url;
    if (currentFilter === "today") {
	    url = "/todos/today";
	} else if (currentFilter === "tomorrow") {
	    url = "/todos/tomorrow";
	} else if (currentFilter === "scheduled") {
	    url = "/todos/scheduled";
	} else if (currentFilter === "all") {
	    url = "/todos";
	} else {
	    url = `/lists/${currentListId}/todos`;
	}		

    fetch(url)
        .then(res => res.json())
        .then(data => {
			todoCache = data;
			hasTasks = data.length > 0;
			updateHeader();
			todoList.innerHTML = "";
			
			let todoGroups = {};
			if (
			    currentFilter === "all" ||
			    currentFilter === "scheduled" ||
			    currentFilter === "today" ||
			    currentFilter === "tomorrow"
			) {		
			    data.forEach(todo => {		
			        const listName = todo.listName;		
			        if (!todoGroups[listName]) {
			            todoGroups[listName] = [];
			        }
			        todoGroups[listName].push(todo);
			    });
			} else {
			    todoGroups[currentListId] = data;
			}

			Object.values(todoGroups).forEach(group => {
				
				if (group.length === 0) {
				   return;
				 }
			
			    if (currentListId === null) {
					const header = document.createElement("h3");
					header.className = "todo-group-title";
					
					const colorDot = document.createElement("span");
					colorDot.className = "todo-group-color";
					colorDot.style.backgroundColor = group[0].listColor;
					
					const name = document.createElement("span");
					name.textContent = group[0].listName;
					
					header.appendChild(colorDot);
					header.appendChild(name);
					todoList.appendChild(header);
			    }
			
			
			    group.forEach(todo => {
					console.log(todo.notify);
					const dragHandle = document.createElement("span");
					dragHandle.classList.add("drag-handle");
					dragHandle.innerHTML = '<i class="fa-solid fa-grip-vertical"></i>';
	                const li = document.createElement("li");
	                li.id = `todo-${todo.id}`;
	                li.classList = "todo-item";
	                li.style.setProperty("--list-color", todo.listColor);
	                li.addEventListener("click", () => {
	    				selectedTodoId = todo.id;
	    				showTodoDetail(todo);
	    				document.querySelectorAll(".todo-item").forEach(item => item.classList.remove("selected"));
	    				li.classList.add("selected");
	    			});
	                const text = document.createElement("div");
	                text.classList.add("text-wrapper");
					
	                const title = document.createElement("div");
					title.classList.add("todo-title");
					title.textContent = todo.title;
					
					title.style.textDecoration =
					    todo.done ? "line-through" : "none";
					
					text.appendChild(title);
					
					if (todo.id === selectedTodoId) {
	    				li.classList.add("selected");
					}
					
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
					    dateTime.textContent = formatDateTime(d);
					
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
	                        if (currentListId !== null) {
       			 				li.appendChild(dragHandle);
    					}
					}
	                todoList.appendChild(li);
	            });
            initTodoSortable();
            });
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
// 日時の成型
// ============================
function formatDateTime(dateTime) {

    const date = new Date(dateTime);

    return date.toLocaleString("ja-JP", {
        year: "numeric",
        month: "numeric",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

// ============================
// 各タスクの詳細画面
// ============================
function showTodoDetail(todo) {
	
	if (!todo) {
		selectedTodoId = null;
        detailTodoTitle.textContent = "";
        detailTodoMemo.textContent = "";
        detailTodoDateTime.textContent = "";
        detailTodoNotify.textContent = "";
        detailDeleteTodoBtn.classList.add("hidden");
        return;
    }

	detailTodoTitle.innerHTML = `
        <span class="editable-title">${todo.title || "タイトルなし"}</span>
    `;

	detailTodoMemo.innerHTML = `
	    <label class="detail-label">メモ</label>
	    <span class="editable-memo">${todo.memo || "＋ メモを追加"}</span>
	`;
    
    const formattedDateTime = todo.dateTime
    ? formatDateTime(todo.dateTime)
    : "＋ 日時を追加";
	
	detailTodoDateTime.innerHTML = `
	    <label class="detail-label">日時</label>
	    <span class="editable-datetime">${formattedDateTime}</span>
	`;
	
	const canNotify = !!todo.dateTime;
	
	detailTodoNotify.innerHTML = `
	    <div class="editable-notify">
	        <span class="detail-label-inline">通知</span>
	
	        <label class="switch">
				<input
				    type="checkbox"
				    id="detail-notify-toggle"
				    ${todo.notify ? "checked" : ""}
				    ${todo.dateTime ? "" : "disabled"}
				>
	            <span class="slider"></span>
	        </label>
	    </div>
	    <span class="notify-hint">
				 ※日時を設定すると通知できます
		</span>
	`;
	
	detailDeleteTodoBtn.classList.remove("hidden");
    
    detailTodoTitle
        .querySelector(".editable-title")
        .addEventListener("click", (e) => {

            e.stopPropagation();

            startInlineEdit({
                element: e.target,
                value: todo.title,
                field: "title",
                todo
            });
        });

    detailTodoMemo
    	.querySelector(".editable-memo")
        .addEventListener("click", (e) => {

            e.stopPropagation();

            startInlineEdit({
                element: e.target,
                value: todo.memo,
                field: "memo",
                todo
            });
        });

    detailTodoDateTime
        .querySelector(".editable-datetime")
        .addEventListener("click", (e) => {

            e.stopPropagation();

            startInlineEdit({
                element: e.target,
                value: todo.dateTime,
                field: "dateTime",
                todo,
                type: "datetime-local"
            });
        });
        
	detailTodoNotify
		.querySelector("#detail-notify-toggle")
		.addEventListener("change", (e) => {
			toggleNotification(todo, e.target.checked);
		});
}

// ============================
// 各タスクの編集
// ============================
function startInlineEdit({element, value, field, todo, type = "text"}) {

	let isSaving = false;
    const input =
        field === "memo"
            ? document.createElement("textarea")
            : document.createElement("input");
    
    if (field !== "memo") {
        input.type = type;
    }
    
    input.value = value || "";
    input.classList.add("inline-edit-input");
    
    if (field === "memo") {
    	const height = element.getBoundingClientRect().height;
    	element.replaceWith(input);
    	if(height === 40){
			input.style.height = '120px';
		} else{
	    	input.style.height = `${height}px`;
	    }
	} else {
		element.replaceWith(input);
	}
    
    input.focus();
    input.addEventListener("input", () => {
    	input.classList.remove("input-error");
	});
    input.addEventListener("blur", save);
    if (field !== "memo") {
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                input.blur();
            }
        });
    }

    input.addEventListener("click", (e) => {
        e.stopPropagation();
    });
    
    function save() {
	    if (isSaving) return;
	    isSaving = true;	
	    const newValue = input.value.trim();
	
	    if (field === "title" && !newValue) {
	        input.classList.add("input-error");
	        input.focus();
	        isSaving = false;
	        return;
	    }

	    fetch(`/todos/${todo.id}`, {
	        method: "PATCH",
	        headers: {
	            "Content-Type": "application/json"
	        },
	        body: JSON.stringify({
	            title:
	                field === "title" ? newValue : todo.title,
	
	            memo:
	                field === "memo" ? newValue : todo.memo,
	
	            dateTime:
	                field === "dateTime" ? newValue : todo.dateTime,
	            notify: todo.notify
	        })
	    })
	    .then(() => {
	        todo[field] = newValue;
	        showTodoDetail(todo);
	        fetchTodos();
	    });
	}
}

function toggleNotification(todo, checked) {

    fetch(`/todos/${todo.id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            title: todo.title,
            memo: todo.memo,
            dateTime: todo.dateTime,
            notify: checked
        })
    })
    .then(() => {

        todo.notify = checked;
        showTodoDetail(todo)
        fetchTodos();
    });
}

// ============================
// 詳細画面からのタスクの削除
// ============================
detailDeleteTodoBtn?.addEventListener("click", () => {
	if (!confirm("このタスクを削除しますか？")) {
	  return;
	 }
	deleteTodo(selectedTodoId);
});

// ============================
// タスク作成モーダルを開く
// ============================
openAddTodoModalBtn.addEventListener("click", () => {
	addTodoInput.value = "";
    addTodoMemo.value = "";
    addTodoDateTime.value = "";
    addTodoNotify.checked = false;
    addTodoNotify.disabled = true;
    addTodoModal.classList.remove("hidden");
});

// ============================
// タスク作成モーダル閉じる
// ============================
addTodoCloseBtn?.addEventListener("click", () => {
    addTodoModal.classList.add("hidden");
});

addTodoDateTime.addEventListener("input", () => {

    const hasDateTime =
        addTodoDateTime.value.trim() !== "";

    addTodoNotify.disabled = !hasDateTime;

    if (!hasDateTime) {
        addTodoNotify.checked = false;
    }
});

// ============================
// タスクの作成
// ============================
addTodoBtn?.addEventListener("click", () => {
    const title = addTodoInput.value.trim();
    const memo = addTodoMemo.value.trim();
    const dateTime = addTodoDateTime.value.trim();
    const notify = addTodoNotify.checked;

    if (!title) return;

    fetch("/todos", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            title,
            memo,
            dateTime: dateTime || null,
            notify,
            listId: currentListId,
            todayAdd: currentFilter === "today" || currentFilter === "scheduled",
            tomorrowAdd: currentFilter === "tomorrow"
        })
    })
    .then(res => res.json())
    .then(() => {
        fetchLists();
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
    addTodoNotify.checked = false;
    addTodoNotify.disabled = true;
}


// ============================
// タスクの削除
// ============================
function deleteTodo(id) {
    fetch(`/todos/${id}`, { method: "DELETE" })
        .then(() => {
        	fetchTodos();
        	if (selectedTodoId === id) { showTodoDetail(null); }
        });
}

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
	console.log(currentListId);
	const url =
	    currentListId === null
	    ? `/todos?done=${done}`
	    : `/lists/${currentListId}/todos?done=${done}`;

    fetch(url, {
        method: "PATCH"
    }).then(() => fetchTodos());
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
		if(bulkMode){
			menuBtn.style.display = "none";
			openAddTodoModalBtn.classList.add("hidden");
		} else {
			if(hasTasks){	
				menuBtn.style.display = "block";
			} else {
				menuBtn.style.display = "none";
			}
			openAddTodoModalBtn.classList.remove("hidden");
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
    openAddTodoModalBtn.classList.remove("hidden");
    fetchTodos();
    updateHeader();
    updateFooter();
});

footerNotDoneBtn?.addEventListener("click", () => {
	updateAllTodoStatus(false);
    bulkMode = false;
    doneMode = false;
    openAddTodoModalBtn.classList.remove("hidden");
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
        openAddTodoModalBtn.classList.remove("hidden");
        fetchTodos();
        updateFooter();
    });
});

// ============================
// タスクの並び替え
// ============================
function initTodoSortable() {
	
	if (currentListId === null) {
	    return;
	}

    if (bulkMode) {

        if (todoSortable) {
            todoSortable.destroy();
            todoSortable = null;
        }

        return;
    }

    if (todoSortable) {
        todoSortable.destroy();
    }

    todoSortable = new Sortable(todoList, {
        animation: 150,
        handle: ".drag-handle",
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
			updateHeader();

            sidebarMylist.innerHTML = "";

            data.forEach(list => {
				const li = document.createElement("li");
				li.id = `list-${list.id}`;
				li.addEventListener("contextmenu", (e) => {
				    e.preventDefault();
				    e.stopPropagation();
				
				    contextTargetListId = list.id;
				
				    listContextMenu.style.left = `${e.pageX}px`;
				    listContextMenu.style.top = `${e.pageY}px`;
				
				    listContextMenu.classList.remove("hidden");
				});
				li.innerHTML = `
				    <i class="fa-solid fa-list list-icon"
				       style="color:${list.color};"></i>
				    ${list.name}
				`;
				
				sidebarMylist.appendChild(li);
                
			li.addEventListener("click", () => {
			    currentListId = Number(list.id);
			    currentFilter = null;
			
			    localStorage.setItem(
			        "currentListId",
			        currentListId
			    );
			    
			    document.querySelectorAll("#sidebar-mylist li").forEach(item => item.classList.remove("active")); 
				document.querySelectorAll("#sidebar-filter li").forEach(item => item.classList.remove("active"));
			
			    li.classList.add("active");
			
			    document.getElementById("current-list-title").textContent = list.name;
			    document.getElementById("current-list-title").style.color = list.color;
			
			    fetchTodos();
        	});
        	initListSortable();
        	if (currentListId === null && currentFilter === null) {
			
			    allFilter
			        .classList.add("active");
			
			    document.getElementById("current-list-title")
			        .textContent = "すべて";
			
			    document.getElementById("current-list-title")
			        .style.color = "";
			
			    fetchTodos();
			}
        });
     });
}

allFilter.addEventListener("click", () => {

	currentFilter = "all";
    currentListId = null;

    localStorage.removeItem("currentListId");
	localStorage.setItem("currentFilter", "all");

    document.querySelectorAll("#sidebar-mylist li").forEach(item => item.classList.remove("active")); 
	document.querySelectorAll("#sidebar-filter li").forEach(item => item.classList.remove("active"));

    allFilter.classList.add("active");

    document.getElementById("current-list-title").textContent = "すべて";
    document.getElementById("current-list-title").style.color = "";
    
    fetchTodos();
});

scheduledFilter.addEventListener("click", () => {

    currentFilter = "scheduled";
    currentListId = null;

    localStorage.removeItem("currentListId");
	localStorage.setItem("currentFilter", "scheduled");

    document.querySelectorAll("#sidebar-mylist li").forEach(item => item.classList.remove("active")); 
	document.querySelectorAll("#sidebar-filter li").forEach(item => item.classList.remove("active"));

    scheduledFilter.classList.add("active");

    document.getElementById("current-list-title").textContent = "日時あり";
    document.getElementById("current-list-title").style.color = "";

    fetchTodos();
});

todayFilter.addEventListener("click", () => {

    currentFilter = "today";
    currentListId = null;

    localStorage.removeItem("currentListId");
    localStorage.setItem("currentFilter", "today");

    document.querySelectorAll("#sidebar-mylist li").forEach(item => item.classList.remove("active")); 
	document.querySelectorAll("#sidebar-filter li").forEach(item => item.classList.remove("active"));

    todayFilter.classList.add("active");

    document.getElementById("current-list-title").textContent = "今日";
    document.getElementById("current-list-title").style.color = "";

    fetchTodos();
});

tomorrowFilter.addEventListener("click", () => {

    currentFilter = "tomorrow";
    currentListId = null;

    localStorage.removeItem("currentListId");
    localStorage.setItem("currentFilter", "tomorrow");

    document.querySelectorAll("#sidebar-mylist li").forEach(item => item.classList.remove("active")); 
	document.querySelectorAll("#sidebar-filter li").forEach(item => item.classList.remove("active"));

    tomorrowFilter.classList.add("active");

    document.getElementById("current-list-title").textContent = "明日";
    document.getElementById("current-list-title").style.color = "";

    fetchTodos();
});

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
    const selectedTodo = todoCache.find(
        todo => todo.id === selectedTodoId
    );

    await fetch(`/lists/${contextTargetListId}`, {
        method: "DELETE"
    });

	if (currentListId === contextTargetListId) {
	
	    currentListId = null;
	
	    localStorage.removeItem("currentListId");
	
	    todoList.innerHTML = "";
	
	    document.getElementById(
	        "current-list-title"
	    ).textContent = "Todo タスク";
	    
	    document.getElementById("current-list-title").style.color = "#000000";
	}
	
    if (selectedTodo?.listId === contextTargetListId) {

        showTodoDetail(null);
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
// リストの並び替え
// ============================
function initListSortable() {
	
	if (listSortable) {
        listSortable.destroy();
    }

    listSortable = new Sortable(sidebarMylist, {
        animation: 150,

        onEnd: () => {

            const orderedIds = [...sidebarMylist.children].map(li =>
                Number(li.id.replace("list-", ""))
            );

            fetch("/lists/reorder", {
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
// タスクの通知を出す
// ============================
async function checkNotifications() {
    if (Notification.permission !== "granted") return;

    const res = await fetch(`/todos`);
    const todos = await res.json();
    const now = new Date();

	todos.forEach(todo => {
	    if (!todo.dateTime) return;
	    if (!todo.notify) return;
	    if (todo.done) return;
	    if (todo.notified) return;
	
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
