/**
 * TASKNEST — Frontend JavaScript
 * Connects to Spring Boot REST APIs using fetch()
 */

// State Management
let currentView = 'all'; // 'all', 'today', 'overdue', 'completed', or list ID
let taskLists = [];
let allTasks = [];
let editingTaskId = null;

// DOM Elements
const taskContainer = document.getElementById('tasks-container');
const emptyState = document.getElementById('empty-state');
const viewTitle = document.getElementById('view-title');
const viewDescription = document.getElementById('view-description');
const searchInput = document.getElementById('search-input');
const taskListsNav = document.getElementById('task-lists-nav');

// Modals
const taskModal = document.getElementById('task-modal');
const modalTitle = document.getElementById('modal-title');
const taskForm = document.getElementById('task-form');
const taskIdInput = document.getElementById('task-id');
const taskTitleInput = document.getElementById('task-title-input');
const taskDescInput = document.getElementById('task-desc-input');
const taskListSelect = document.getElementById('task-list-select');
const taskPrioritySelect = document.getElementById('task-priority-select');
const taskDueDateInput = document.getElementById('task-due-date');

// Move Modal
const moveModal = document.getElementById('move-modal');
const moveTaskId = document.getElementById('move-task-id');
const moveTaskName = document.getElementById('move-task-name');
const moveTargetList = document.getElementById('move-target-list');

// Add List Inline Form
const addListContainer = document.getElementById('add-list-container');
const newListNameInput = document.getElementById('new-list-name');

// ----------------------------------------------------
// Initialization
// ----------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadTaskLists().then(() => {
        loadTasks();
        refreshBadgesAndStats();
    });
});

function setupEventListeners() {
    // Primary Nav View clicks
    document.querySelectorAll('.nav-item[data-view]').forEach(item => {
        item.addEventListener('click', (e) => {
            const view = item.getAttribute('data-view');
            switchView(view);
        });
    });

    // Add List Toggle Buttons
    document.getElementById('btn-add-list').addEventListener('click', () => {
        addListContainer.style.display = addListContainer.style.display === 'none' ? 'block' : 'none';
        if (addListContainer.style.display === 'block') {
            newListNameInput.focus();
        }
    });

    document.getElementById('btn-cancel-list').addEventListener('click', () => {
        addListContainer.style.display = 'none';
        newListNameInput.value = '';
    });

    document.getElementById('btn-save-list').addEventListener('click', handleCreateList);
    newListNameInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleCreateList();
        }
    });

    // Add Task Button
    document.getElementById('btn-new-task').addEventListener('click', () => {
        openTaskModal();
    });

    // Task Modal Buttons
    document.getElementById('modal-close-btn').addEventListener('click', closeTaskModal);
    document.getElementById('modal-cancel-btn').addEventListener('click', closeTaskModal);
    taskForm.addEventListener('submit', handleTaskFormSubmit);

    // Move Modal Buttons
    document.getElementById('move-close-btn').addEventListener('click', closeMoveModal);
    document.getElementById('move-cancel-btn').addEventListener('click', closeMoveModal);
    document.getElementById('move-confirm-btn').addEventListener('click', handleMoveTaskConfirm);

    // Search Input
    searchInput.addEventListener('input', (e) => {
        renderTasks(filterTasksBySearch(allTasks, e.target.value));
    });
}

// ----------------------------------------------------
// View Navigation
// ----------------------------------------------------
function switchView(viewKey, listName = '') {
    currentView = viewKey;

    // Update active nav class
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

    const activeNav = document.querySelector(`.nav-item[data-view="${viewKey}"]`);
    if (activeNav) {
        activeNav.classList.add('active');
    }

    // Set Header Titles
    if (viewKey === 'all') {
        viewTitle.innerText = 'All Tasks';
        viewDescription.innerText = 'Manage and organize all your daily to-dos and priorities.';
    } else if (viewKey === 'today') {
        viewTitle.innerText = "Today's Tasks";
        viewDescription.innerText = 'Tasks scheduled for completion today.';
    } else if (viewKey === 'overdue') {
        viewTitle.innerText = 'Overdue Tasks';
        viewDescription.innerText = 'Incomplete tasks whose due date has passed.';
    } else if (viewKey === 'completed') {
        viewTitle.innerText = 'Completed Tasks';
        viewDescription.innerText = 'Tasks you have successfully finished.';
    } else if (viewKey.startsWith('list-')) {
        viewTitle.innerText = listName || 'Task List';
        viewDescription.innerText = `Tasks filed under category: ${listName}`;
    }

    loadTasks();
}

// ----------------------------------------------------
// API Calls: Task Lists
// ----------------------------------------------------
async function loadTaskLists() {
    try {
        const response = await fetch('/api/lists');
        if (!response.ok) throw new Error('Failed to fetch task lists');
        taskLists = await response.json();
        renderTaskListsNav();
        populateListDropdowns();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function renderTaskListsNav() {
    taskListsNav.innerHTML = '';
    taskLists.forEach(list => {
        const li = document.createElement('li');
        li.className = `nav-item ${currentView === `list-${list.id}` ? 'active' : ''}`;
        li.setAttribute('data-view', `list-${list.id}`);

        li.innerHTML = `
            <span class="nav-icon">📁</span>
            <span class="nav-label">${escapeHtml(list.name)}</span>
            <span class="badge count-badge">${list.taskCount}</span>
            <button class="list-item-delete" title="Delete list">&times;</button>
        `;

        // Click to view list
        li.addEventListener('click', (e) => {
            if (e.target.classList.contains('list-item-delete')) {
                e.stopPropagation();
                handleDeleteList(list.id, list.name);
                return;
            }
            switchView(`list-${list.id}`, list.name);
        });

        taskListsNav.appendChild(li);
    });
}

function populateListDropdowns() {
    // Task modal list dropdown
    taskListSelect.innerHTML = '';
    moveTargetList.innerHTML = '';

    taskLists.forEach(list => {
        const opt1 = document.createElement('option');
        opt1.value = list.id;
        opt1.textContent = list.name;
        taskListSelect.appendChild(opt1);

        const opt2 = document.createElement('option');
        opt2.value = list.id;
        opt2.textContent = list.name;
        moveTargetList.appendChild(opt2);
    });
}

async function handleCreateList() {
    const name = newListNameInput.value.trim();
    if (!name) {
        showToast('List name cannot be empty', 'error');
        return;
    }

    try {
        const response = await fetch('/api/lists', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Error creating task list');
        }

        const newList = await response.json();
        showToast(`Created list "${newList.name}"`, 'success');
        newListNameInput.value = '';
        addListContainer.style.display = 'none';

        await loadTaskLists();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleDeleteList(listId, listName) {
    if (!confirm(`Are you sure you want to delete "${listName}"? All tasks inside it will also be deleted.`)) {
        return;
    }

    try {
        const response = await fetch(`/api/lists/${listId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'Error deleting task list');
        }

        showToast(`Deleted list "${listName}"`, 'success');
        if (currentView === `list-${listId}`) {
            switchView('all');
        }
        await loadTaskLists();
        await loadTasks();
        refreshBadgesAndStats();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ----------------------------------------------------
// API Calls: Tasks
// ----------------------------------------------------
async function loadTasks() {
    let url = '/api/tasks';

    if (currentView === 'today') {
        url = '/api/tasks/today';
    } else if (currentView === 'overdue') {
        url = '/api/tasks/overdue';
    } else if (currentView === 'completed') {
        url = '/api/tasks/completed';
    } else if (currentView.startsWith('list-')) {
        const listId = currentView.split('-')[1];
        url = `/api/tasks/list/${listId}`;
    }

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to load tasks');
        allTasks = await response.json();
        
        const filtered = filterTasksBySearch(allTasks, searchInput.value);
        renderTasks(filtered);
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function filterTasksBySearch(tasks, query) {
    if (!query || !query.trim()) return tasks;
    const q = query.toLowerCase().trim();
    return tasks.filter(t => 
        (t.title && t.title.toLowerCase().includes(q)) ||
        (t.description && t.description.toLowerCase().includes(q)) ||
        (t.taskListName && t.taskListName.toLowerCase().includes(q)) ||
        (t.priority && t.priority.toLowerCase().includes(q))
    );
}

function renderTasks(tasks) {
    taskContainer.innerHTML = '';

    if (!tasks || tasks.length === 0) {
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    tasks.forEach(task => {
        const card = createTaskCardElement(task);
        taskContainer.appendChild(card);
    });
}

function createTaskCardElement(task) {
    const card = document.createElement('div');
    card.className = `task-card ${task.completed ? 'is-completed' : ''} ${task.overdue ? 'is-overdue' : ''}`;
    card.id = `task-card-${task.id}`;

    // Priority badge class
    const priorityClass = `priority-${task.priority}`;

    // Due Date text and styling
    let dueDateHtml = '';
    if (task.dueDate) {
        const todayStr = new Date().toISOString().split('T')[0];
        let dueClass = '';
        let dueLabel = task.dueDate;

        if (task.dueDate === todayStr) {
            dueClass = 'due-today';
            dueLabel = 'Today';
        } else if (task.overdue) {
            dueClass = 'due-overdue';
            dueLabel = `${task.dueDate} (Overdue)`;
        }

        dueDateHtml = `<span class="meta-tag tag-due ${dueClass}">📅 ${dueLabel}</span>`;
    }

    const listTagHtml = `<span class="meta-tag tag-list">📁 ${escapeHtml(task.taskListName || 'General')}</span>`;
    const priorityBadgeHtml = `<span class="priority-badge ${priorityClass}">${task.priority}</span>`;

    card.innerHTML = `
        <div class="task-checkbox-wrapper">
            <div class="custom-checkbox ${task.completed ? 'checked' : ''}" 
                 onclick="toggleTaskStatus(${task.id}, ${task.completed})"
                 title="${task.completed ? 'Mark as Incomplete' : 'Mark as Complete'}">
            </div>
        </div>
        <div class="task-body">
            <div class="task-title-row">
                <span class="task-title">${escapeHtml(task.title)}</span>
                ${priorityBadgeHtml}
            </div>
            ${task.description ? `<p class="task-desc">${escapeHtml(task.description)}</p>` : ''}
            <div class="task-meta-row">
                ${listTagHtml}
                ${dueDateHtml}
            </div>
        </div>
        <div class="task-actions">
            <button class="btn-card-action" onclick="openMoveModal(${task.id}, '${escapeQuote(task.title)}', ${task.taskListId})" title="Move to another list">⇄ Move</button>
            <button class="btn-card-action" onclick="openEditTaskModal(${task.id})" title="Edit task">✎ Edit</button>
            <button class="btn-card-action btn-card-delete" onclick="handleDeleteTask(${task.id})" title="Delete task">🗑 Delete</button>
        </div>
    `;

    return card;
}

// ----------------------------------------------------
// Task Actions: Complete / Incomplete / Delete
// ----------------------------------------------------
async function toggleTaskStatus(taskId, currentStatus) {
    const endpoint = currentStatus ? `/api/tasks/${taskId}/incomplete` : `/api/tasks/${taskId}/complete`;

    try {
        const response = await fetch(endpoint, { method: 'PUT' });
        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Error updating task status');
        }

        showToast(currentStatus ? 'Task marked as incomplete' : 'Task marked as complete! 🎉', 'success');
        await loadTasks();
        await loadTaskLists();
        refreshBadgesAndStats();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function handleDeleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }

    try {
        const response = await fetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'Error deleting task');
        }

        showToast('Task deleted successfully', 'success');
        await loadTasks();
        await loadTaskLists();
        refreshBadgesAndStats();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ----------------------------------------------------
// Task Modal: Create / Edit
// ----------------------------------------------------
function openTaskModal() {
    if (taskLists.length === 0) {
        showToast('Please create at least one task list first.', 'error');
        addListContainer.style.display = 'block';
        newListNameInput.focus();
        return;
    }

    editingTaskId = null;
    taskIdInput.value = '';
    modalTitle.innerText = 'Add New Task';
    taskForm.reset();

    // Default task list if in list view
    if (currentView.startsWith('list-')) {
        const activeListId = currentView.split('-')[1];
        taskListSelect.value = activeListId;
    }

    taskModal.style.display = 'flex';
    taskTitleInput.focus();
}

async function openEditTaskModal(taskId) {
    try {
        const response = await fetch(`/api/tasks/${taskId}`);
        if (!response.ok) throw new Error('Failed to load task details');
        const task = await response.json();

        editingTaskId = task.id;
        taskIdInput.value = task.id;
        modalTitle.innerText = 'Edit Task';
        taskTitleInput.value = task.title;
        taskDescInput.value = task.description || '';
        taskPrioritySelect.value = task.priority;
        taskListSelect.value = task.taskListId;
        taskDueDateInput.value = task.dueDate || '';

        taskModal.style.display = 'flex';
        taskTitleInput.focus();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function closeTaskModal() {
    taskModal.style.display = 'none';
    taskForm.reset();
    editingTaskId = null;
}

async function handleTaskFormSubmit(e) {
    e.preventDefault();

    const title = taskTitleInput.value.trim();
    const description = taskDescInput.value.trim();
    const taskListId = parseInt(taskListSelect.value);
    const priority = taskPrioritySelect.value;
    const dueDate = taskDueDateInput.value || null;

    if (!title) {
        showToast('Task title cannot be empty', 'error');
        return;
    }

    const payload = {
        title,
        description,
        taskListId,
        priority,
        dueDate
    };

    try {
        let response;
        if (editingTaskId) {
            response = await fetch(`/api/tasks/${editingTaskId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            response = await fetch('/api/tasks', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Error saving task');
        }

        showToast(editingTaskId ? 'Task updated successfully' : 'Task created successfully', 'success');
        closeTaskModal();
        await loadTasks();
        await loadTaskLists();
        refreshBadgesAndStats();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ----------------------------------------------------
// Move Task Modal
// ----------------------------------------------------
function openMoveModal(taskId, title, currentListId) {
    moveTaskId.value = taskId;
    moveTaskName.innerText = title;
    moveTargetList.value = currentListId;
    moveModal.style.display = 'flex';
}

function closeMoveModal() {
    moveModal.style.display = 'none';
}

async function handleMoveTaskConfirm() {
    const taskId = moveTaskId.value;
    const targetListId = moveTargetList.value;

    try {
        const response = await fetch(`/api/tasks/${taskId}/move/${targetListId}`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Error moving task');
        }

        showToast('Task moved successfully', 'success');
        closeMoveModal();
        await loadTasks();
        await loadTaskLists();
        refreshBadgesAndStats();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ----------------------------------------------------
// Statistics & Badges Refresh
// ----------------------------------------------------
async function refreshBadgesAndStats() {
    try {
        const [allRes, todayRes, overdueRes, completedRes] = await Promise.all([
            fetch('/api/tasks'),
            fetch('/api/tasks/today'),
            fetch('/api/tasks/overdue'),
            fetch('/api/tasks/completed')
        ]);

        const all = await allRes.json();
        const today = await todayRes.json();
        const overdue = await overdueRes.json();
        const completed = await completedRes.json();

        // Update Nav Badges
        document.getElementById('count-all').innerText = all.length;
        document.getElementById('count-today').innerText = today.length;
        document.getElementById('count-overdue').innerText = overdue.length;
        document.getElementById('count-completed').innerText = completed.length;

        // Update Stat Cards
        document.getElementById('stat-total').innerText = all.length;
        document.getElementById('stat-today').innerText = today.length;
        document.getElementById('stat-overdue').innerText = overdue.length;
        document.getElementById('stat-completed').innerText = completed.length;
    } catch (error) {
        console.error('Error updating stats:', error);
    }
}

// ----------------------------------------------------
// Toast Notification Utility
// ----------------------------------------------------
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ----------------------------------------------------
// String Escaping Utilities
// ----------------------------------------------------
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function escapeQuote(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
