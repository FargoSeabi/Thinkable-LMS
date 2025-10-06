// Admin Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    checkAuth();
    
    // Initialize navigation
    initNavigation();
    
    // Initialize mobile menu
    initMobileMenu();
    
    // Load initial data
    loadDashboardData();
    
    // Setup forms
    setupForms();
    
    // Setup table interactions
    setupTableInteractions();
});

// Authentication Check
function checkAuth() {
    const email = localStorage.getItem('userEmail');
    const role = localStorage.getItem('userRole');
    
    if (!email || role !== 'ADMIN') {
        window.location.href = '/index.html';
        return;
    }
    
    // Update admin info
    document.querySelector('.admin-name').textContent = email;
}

// Navigation Management
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.content-section');
    
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            
            const sectionName = item.dataset.section;
            
            // Update navigation
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
            
            // Update sections
            sections.forEach(section => section.classList.remove('active'));
            document.getElementById(`${sectionName}-section`).classList.add('active');
            
            // Update page title
            const title = item.querySelector('span').textContent;
            document.querySelector('.page-title').textContent = title;
            
            // Load section data
            loadSectionData(sectionName);
        });
    });
}

// Mobile Menu
function initMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.querySelector('.sidebar');
    
    mobileMenuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('show');
    });
    
    // Close sidebar when clicking outside
    document.addEventListener('click', (e) => {
        if (!sidebar.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
            sidebar.classList.remove('show');
        }
    });
}

// Load Section Data
function loadSectionData(section) {
    switch(section) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'students':
            loadStudents();
            break;
        case 'lessons':
            loadLessons();
            break;
        case 'books':
            loadBooks();
            break;
        case 'quizzes':
            loadQuizzes();
            break;
    }
}

// Dashboard Data
async function loadDashboardData() {
    try {
        const email = localStorage.getItem('userEmail');
        
        // Load all data for stats
        const [students, lessons, books, quizzes] = await Promise.all([
            fetch(`http://localhost:8081/api/admin/users?email=${encodeURIComponent(email)}`).then(r => r.json()),
            fetch(`http://localhost:8081/api/admin/lessons?email=${encodeURIComponent(email)}`).then(r => r.json()),
            fetch(`http://localhost:8081/api/admin/books?email=${encodeURIComponent(email)}`).then(r => r.json()),
            fetch(`http://localhost:8081/api/admin/quizzes?email=${encodeURIComponent(email)}`).then(r => r.json())
        ]);
        
        // Update stats
        document.getElementById('totalStudents').textContent = students.length;
        document.getElementById('totalLessons').textContent = lessons.length;
        document.getElementById('totalBooks').textContent = books.length;
        document.getElementById('totalQuizzes').textContent = quizzes.length;
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showMessage('Error loading dashboard data', 'error');
    }
}

// Students Management
async function loadStudents() {
    try {
        const email = localStorage.getItem('userEmail');
        const response = await fetch(`http://localhost:8081/api/admin/users?email=${encodeURIComponent(email)}`);
        
        if (!response.ok) throw new Error('Failed to fetch students');
        
        const students = await response.json();
        const tbody = document.querySelector('#studentTable tbody');
        tbody.innerHTML = '';
        
        students.forEach(student => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <input type="checkbox" class="student-checkbox" value="${student.email}">
                </td>
                <td>${student.name || 'N/A'}</td>
                <td>${student.email}</td>
                <td>
                    <div class="progress-container">
                        <div class="progress-bar" style="width: ${student.progress || 0}%"></div>
                    </div>
                    <small>${student.progress || 0}%</small>
                </td>
                <td>${student.lastActive || 'Never'}</td>
                <td>
                    <button class="edit-btn" onclick="editStudent('${student.email}')" title="Edit Student">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="view-btn" onclick="updatePassword('${student.email}')" title="Update Password">
                        <i class="fas fa-key"></i>
                    </button>
                    <button class="delete-btn" onclick="deleteStudent('${student.email}')" title="Delete Student">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
    } catch (error) {
        console.error('Error loading students:', error);
        showMessage('Error loading students', 'error', 'studentMessage');
    }
}

// Lessons Management
async function loadLessons() {
    try {
        const email = localStorage.getItem('userEmail');
        const response = await fetch(`http://localhost:8081/api/admin/lessons?email=${encodeURIComponent(email)}`);
        
        if (!response.ok) throw new Error('Failed to fetch lessons');
        
        const lessons = await response.json();
        const tbody = document.querySelector('#lessonTable tbody');
        tbody.innerHTML = '';
        
        lessons.forEach(lesson => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${lesson.title}</td>
                <td>${lesson.description}</td>
                <td>
                    <a href="${lesson.youtubeUrl}" target="_blank" class="text-truncate">
                        ${lesson.youtubeUrl}
                    </a>
                </td>
                <td>
                    <button class="delete-btn" onclick="deleteLesson(${lesson.id})" title="Delete Lesson">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
        // Update lesson options for quiz form
        updateLessonOptions(lessons);
        
    } catch (error) {
        console.error('Error loading lessons:', error);
        showMessage('Error loading lessons', 'error', 'lessonMessage');
    }
}

// Books Management
async function loadBooks() {
    try {
        const email = localStorage.getItem('userEmail');
        const response = await fetch(`http://localhost:8081/api/admin/books?email=${encodeURIComponent(email)}`);
        
        if (!response.ok) throw new Error('Failed to fetch books');
        
        const books = await response.json();
        const tbody = document.querySelector('#bookTable tbody');
        tbody.innerHTML = '';
        
        books.forEach(book => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${book.title}</td>
                <td>${book.author}</td>
                <td>
                    <button class="view-btn" onclick="generateAIQuiz(${book.id})" title="Generate AI Quiz">
                        <i class="fas fa-robot"></i>
                    </button>
                    <button class="delete-btn" onclick="deleteBook(${book.id})" title="Delete Book">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
    } catch (error) {
        console.error('Error loading books:', error);
        showMessage('Error loading books', 'error', 'bookMessage');
    }
}

// Quizzes Management
async function loadQuizzes() {
    try {
        const email = localStorage.getItem('userEmail');
        const response = await fetch(`http://localhost:8081/api/admin/quizzes?email=${encodeURIComponent(email)}`);
        
        if (!response.ok) throw new Error('Failed to fetch quizzes');
        
        const quizzes = await response.json();
        const tbody = document.querySelector('#quizTable tbody');
        tbody.innerHTML = '';
        
        if (quizzes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">No quizzes found. Create your first quiz!</td></tr>';
            return;
        }
        
        quizzes.forEach(quiz => {
            const row = document.createElement('tr');
            const lessonTitle = quiz.lesson ? quiz.lesson.title : (quiz.book ? quiz.book.title : 'N/A');
            const questionCount = quiz.questions ? quiz.questions.length : 0;
            
            row.innerHTML = `
                <td>${quiz.title}</td>
                <td>${lessonTitle}</td>
                <td>${questionCount} questions</td>
                <td>
                    <button class="view-btn" onclick="viewQuiz(${quiz.id})" title="View Quiz">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="delete-btn" onclick="deleteQuiz(${quiz.id})" title="Delete Quiz">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
    } catch (error) {
        console.error('Error loading quizzes:', error);
        const tbody = document.querySelector('#quizTable tbody');
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-error">Error loading quizzes</td></tr>';
        showMessage('Error loading quizzes', 'error', 'quizMessage');
    }
}

// Form Setup
function setupForms() {
    // Student Form
    setupStudentForm();
    
    // Lesson Form
    setupLessonForm();
    
    // Book Form  
    setupBookForm();
    
    // Quiz Form
    setupQuizForm();
}

function setupStudentForm() {
    const form = document.getElementById('addStudentForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const adminEmail = localStorage.getItem('userEmail');
        const name = document.getElementById('studentName').value.trim();
        const email = document.getElementById('studentEmail').value.trim();
        const password = document.getElementById('studentPassword').value;
        
        try {
            const response = await fetch('http://localhost:8081/api/admin/add-user', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    adminEmail, 
                    name, 
                    email, 
                    password,
                    role: 'STUDENT'
                })
            });
            
            const result = await response.json();
            
            if (response.ok) {
                showMessage('Student added successfully!', 'success', 'studentMessage');
                form.reset();
                closeModal('addStudentModal');
                loadStudents();
                loadDashboardData(); // Update stats
            } else {
                showMessage(result.message || 'Error adding student', 'error', 'studentMessage');
            }
        } catch (error) {
            console.error('Add student error:', error);
            showMessage('Network error: Unable to connect to server', 'error', 'studentMessage');
        }
    });
}

function setupLessonForm() {
    const form = document.getElementById('addLessonForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const adminEmail = localStorage.getItem('userEmail');
        const title = document.getElementById('lessonTitle').value.trim();
        const description = document.getElementById('lessonDescription').value.trim();
        const youtubeUrl = document.getElementById('youtubeUrl').value.trim();
        
        try {
            const response = await fetch('http://localhost:8081/api/admin/add-lesson', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ adminEmail, title, description, youtubeUrl })
            });
            
            if (response.ok) {
                showMessage('Lesson added successfully!', 'success', 'lessonMessage');
                form.reset();
                closeModal('addLessonModal');
                loadLessons();
                loadDashboardData(); // Update stats
            } else {
                showMessage('Error adding lesson', 'error', 'lessonMessage');
            }
        } catch (error) {
            console.error('Add lesson error:', error);
            showMessage('Network error', 'error', 'lessonMessage');
        }
    });
}

function setupBookForm() {
    const form = document.getElementById('addBookForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const adminEmail = localStorage.getItem('userEmail');
        const title = document.getElementById('bookTitle').value.trim();
        const author = document.getElementById('bookAuthor').value.trim();
        const pdfFile = document.getElementById('bookPdf').files[0];
        
        // File size validation (20MB limit)
        if (pdfFile && pdfFile.size > 20 * 1024 * 1024) {
            showMessage('File size exceeds 20MB limit', 'error', 'bookMessage');
            return;
        }
        
        const formData = new FormData();
        formData.append('adminEmail', adminEmail);
        formData.append('title', title);
        formData.append('author', author);
        formData.append('pdf', pdfFile);
        
        try {
            const response = await fetch('http://localhost:8081/api/admin/add-book', {
                method: 'POST',
                body: formData
            });
            
            const result = await response.json();
            
            if (response.ok) {
                showMessage('Book added successfully!', 'success', 'bookMessage');
                form.reset();
                closeModal('addBookModal');
                loadBooks();
                loadDashboardData(); // Update stats
            } else {
                showMessage(result.message || 'Error adding book', 'error', 'bookMessage');
            }
        } catch (error) {
            console.error('Add book error:', error);
            showMessage('Network error', 'error', 'bookMessage');
        }
    });
}

function setupQuizForm() {
    const form = document.getElementById('addQuizForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const adminEmail = localStorage.getItem('userEmail');
        const quizType = document.getElementById('quizType').value;
        const title = document.getElementById('quizTitle').value.trim();
        
        // Get lesson or book ID based on type
        let lessonId = null;
        let bookId = null;
        
        if (quizType === 'lesson') {
            lessonId = parseInt(document.getElementById('quizLessonId').value);
        } else if (quizType === 'book') {
            bookId = parseInt(document.getElementById('quizBookId').value);
        } else {
            showMessage('Please select quiz type (lesson or book)', 'error', 'quizMessage');
            return;
        }
        
        const questions = Array.from(document.querySelectorAll('.quiz-question-group')).map(group => ({
            question: group.querySelector('.question-text').value,
            options: Array.from(group.querySelectorAll('.option')).map(opt => opt.value),
            correctOption: parseInt(group.querySelector('.correct-option').value) - 1
        }));
        
        try {
            const requestBody = { 
                adminEmail, 
                title, 
                questions 
            };
            
            if (lessonId) {
                requestBody.lessonId = lessonId;
            } else if (bookId) {
                requestBody.bookId = bookId;
            }
            
            const response = await fetch('http://localhost:8081/api/admin/add-quiz', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody)
            });
            
            const result = await response.json();
            
            if (response.ok) {
                showMessage('Quiz added successfully!', 'success', 'quizMessage');
                form.reset();
                resetQuizQuestions();
                resetQuizForm();
                closeModal('addQuizModal');
                loadQuizzes();
                loadDashboardData(); // Update stats
            } else {
                showMessage(result.message || 'Error adding quiz', 'error', 'quizMessage');
            }
        } catch (error) {
            console.error('Add quiz error:', error);
            showMessage('Network error', 'error', 'quizMessage');
        }
    });
}

// Table Interactions
function setupTableInteractions() {
    // Student table select all
    const selectAllStudents = document.getElementById('selectAllStudents');
    selectAllStudents.addEventListener('change', function() {
        const checkboxes = document.querySelectorAll('.student-checkbox');
        const deleteBtn = document.getElementById('deleteSelectedBtn');
        
        checkboxes.forEach(checkbox => {
            checkbox.checked = this.checked;
        });
        
        deleteBtn.style.display = this.checked ? 'inline-flex' : 'none';
    });
    
    // Individual checkbox change
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('student-checkbox')) {
            const checkboxes = document.querySelectorAll('.student-checkbox');
            const checkedBoxes = document.querySelectorAll('.student-checkbox:checked');
            const deleteBtn = document.getElementById('deleteSelectedBtn');
            const selectAll = document.getElementById('selectAllStudents');
            
            selectAll.checked = checkboxes.length === checkedBoxes.length;
            deleteBtn.style.display = checkedBoxes.length > 0 ? 'inline-flex' : 'none';
        }
    });
}

// Modal Functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
    
    // Load quiz options when opening quiz modal
    if (modalId === 'addQuizModal') {
        loadQuizOptions();
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('show');
    document.body.style.overflow = '';
    
    // Clear messages
    const message = modal.querySelector('.form-message');
    if (message) {
        message.classList.remove('show');
        message.textContent = '';
    }
    
    // Reset quiz form when closing quiz modal
    if (modalId === 'addQuizModal') {
        resetQuizForm();
    }
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        const modalId = e.target.id;
        closeModal(modalId);
    }
});

// Student Actions
async function editStudent(email) {
    const name = prompt('Enter new name for student:');
    if (!name) return;
    
    const adminEmail = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch('http://localhost:8081/api/admin/update-user', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ adminEmail, email, name })
        });
        
        if (response.ok) {
            showMessage('Student updated successfully', 'success');
            loadStudents();
        } else {
            showMessage('Error updating student', 'error');
        }
    } catch (error) {
        console.error('Update error:', error);
        showMessage('Network error', 'error');
    }
}

async function updatePassword(email) {
    const newPassword = prompt('Enter new password for student:');
    if (!newPassword) return;
    
    const adminEmail = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch('http://localhost:8081/api/admin/update-password', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ adminEmail, email, password: newPassword })
        });
        
        if (response.ok) {
            showMessage('Password updated successfully', 'success');
        } else {
            showMessage('Error updating password', 'error');
        }
    } catch (error) {
        console.error('Password update error:', error);
        showMessage('Network error', 'error');
    }
}

async function deleteStudent(email) {
    if (!confirm(`Delete student ${email}?`)) return;
    
    const adminEmail = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch('http://localhost:8081/api/admin/delete-user', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ adminEmail, email })
        });
        
        if (response.ok) {
            showMessage('Student deleted successfully', 'success');
            loadStudents();
            loadDashboardData(); // Update stats
        } else {
            showMessage('Failed to delete student', 'error');
        }
    } catch (error) {
        console.error('Delete error:', error);
        showMessage('Error deleting student', 'error');
    }
}

// Bulk Actions
function bulkActions(action) {
    const selected = Array.from(document.querySelectorAll('.student-checkbox:checked'))
                          .map(checkbox => checkbox.value);
    
    if (selected.length === 0) {
        alert('Please select at least one student');
        return;
    }
    
    if (action === 'delete' && confirm(`Delete ${selected.length} selected students?`)) {
        selected.forEach(email => deleteStudent(email));
    }
}

// Other Actions
async function deleteLesson(lessonId) {
    if (!confirm('Delete this lesson?')) return;
    
    const email = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch(`http://localhost:8081/api/admin/delete-lesson/${lessonId}?email=${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showMessage('Lesson deleted successfully', 'success');
            loadLessons();
            loadDashboardData(); // Update stats
        } else {
            showMessage('Failed to delete lesson', 'error');
        }
    } catch (error) {
        console.error('Delete lesson error:', error);
        showMessage('Error deleting lesson', 'error');
    }
}

async function deleteBook(bookId) {
    if (!confirm('Delete this book?')) return;
    
    const email = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch(`http://localhost:8081/api/admin/delete-book/${bookId}?email=${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showMessage('Book deleted successfully', 'success');
            loadBooks();
            loadDashboardData(); // Update stats
        } else {
            showMessage('Failed to delete book', 'error');
        }
    } catch (error) {
        console.error('Delete book error:', error);
        showMessage('Error deleting book', 'error');
    }
}

// Quiz Actions
async function deleteQuiz(quizId) {
    if (!confirm('Delete this quiz? This action cannot be undone.')) return;
    
    const email = localStorage.getItem('userEmail');
    
    try {
        const response = await fetch(`http://localhost:8081/api/admin/delete-quiz/${quizId}?email=${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showMessage('Quiz deleted successfully', 'success');
            loadQuizzes();
            loadDashboardData(); // Update stats
        } else {
            showMessage('Failed to delete quiz', 'error');
        }
    } catch (error) {
        console.error('Delete quiz error:', error);
        showMessage('Error deleting quiz', 'error');
    }
}

async function viewQuiz(quizId) {
    try {
        const response = await fetch(`http://localhost:8081/api/admin/quiz/${quizId}`);
        
        if (!response.ok) throw new Error('Failed to fetch quiz details');
        
        const quiz = await response.json();
        showQuizDetails(quiz);
        
    } catch (error) {
        console.error('View quiz error:', error);
        showMessage('Error loading quiz details', 'error');
    }
}

function showQuizDetails(quiz) {
    const lessonTitle = quiz.lesson ? quiz.lesson.title : (quiz.book ? quiz.book.title : 'N/A');
    
    let questionsHtml = '';
    if (quiz.questions && quiz.questions.length > 0) {
        questionsHtml = quiz.questions.map((question, index) => `
            <div class="quiz-question-detail">
                <h6>Question ${index + 1}: ${question.question}</h6>
                <ul class="question-options">
                    ${question.options.map((option, optIndex) => `
                        <li class="${optIndex === question.correctOption ? 'correct-option' : ''}">${option}</li>
                    `).join('')}
                </ul>
            </div>
        `).join('');
    } else {
        questionsHtml = '<p>No questions found for this quiz.</p>';
    }
    
    const modalContent = `
        <div class="quiz-details">
            <h4>${quiz.title}</h4>
            <p><strong>Associated with:</strong> ${lessonTitle}</p>
            <p><strong>Total Questions:</strong> ${quiz.questions ? quiz.questions.length : 0}</p>
            <div class="questions-section">
                <h5>Questions:</h5>
                ${questionsHtml}
            </div>
        </div>
    `;
    
    // Create and show modal
    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
        <div class="modal-content large">
            <div class="modal-header">
                <h3><i class="fas fa-eye"></i> Quiz Details</h3>
                <button class="modal-close" onclick="this.closest('.modal').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-form">
                ${modalContent}
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';
    
    // Close when clicking outside
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.remove();
            document.body.style.overflow = '';
        }
    });
}

// Quiz Functions
function addQuizQuestion() {
    const questionsDiv = document.getElementById('quizQuestions');
    const count = questionsDiv.children.length + 1;
    
    const questionGroup = document.createElement('div');
    questionGroup.className = 'quiz-question-group';
    questionGroup.innerHTML = `
        <div class="question-header">
            <h5>Question ${count}</h5>
            <button type="button" class="remove-question" onclick="removeQuizQuestion(this)">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="form-group">
            <label>Question Text</label>
            <input type="text" class="question-text" required>
        </div>
        <div class="options-grid">
            <div class="form-group">
                <label>Option 1</label>
                <input type="text" class="option" required>
            </div>
            <div class="form-group">
                <label>Option 2</label>
                <input type="text" class="option" required>
            </div>
            <div class="form-group">
                <label>Option 3</label>
                <input type="text" class="option" required>
            </div>
            <div class="form-group">
                <label>Option 4</label>
                <input type="text" class="option" required>
            </div>
        </div>
        <div class="form-group">
            <label>Correct Answer (1-4)</label>
            <select class="correct-option" required>
                <option value="">Select correct option</option>
                <option value="1">Option 1</option>
                <option value="2">Option 2</option>
                <option value="3">Option 3</option>
                <option value="4">Option 4</option>
            </select>
        </div>
    `;
    
    questionsDiv.appendChild(questionGroup);
    updateRemoveButtons();
}

function removeQuizQuestion(button) {
    const questionGroup = button.closest('.quiz-question-group');
    questionGroup.remove();
    
    // Update question numbers
    const questions = document.querySelectorAll('.quiz-question-group');
    questions.forEach((group, index) => {
        group.querySelector('h5').textContent = `Question ${index + 1}`;
    });
    
    updateRemoveButtons();
}

function updateRemoveButtons() {
    const removeButtons = document.querySelectorAll('.remove-question');
    removeButtons.forEach((button, index) => {
        button.style.display = removeButtons.length > 1 ? 'flex' : 'none';
    });
}

function resetQuizQuestions() {
    document.getElementById('quizQuestions').innerHTML = `
        <div class="quiz-question-group">
            <div class="question-header">
                <h5>Question 1</h5>
                <button type="button" class="remove-question" onclick="removeQuizQuestion(this)" style="display: none;">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="form-group">
                <label>Question Text</label>
                <input type="text" class="question-text" required>
            </div>
            <div class="options-grid">
                <div class="form-group">
                    <label>Option 1</label>
                    <input type="text" class="option" required>
                </div>
                <div class="form-group">
                    <label>Option 2</label>
                    <input type="text" class="option" required>
                </div>
                <div class="form-group">
                    <label>Option 3</label>
                    <input type="text" class="option" required>
                </div>
                <div class="form-group">
                    <label>Option 4</label>
                    <input type="text" class="option" required>
                </div>
            </div>
            <div class="form-group">
                <label>Correct Answer (1-4)</label>
                <select class="correct-option" required>
                    <option value="">Select correct option</option>
                    <option value="1">Option 1</option>
                    <option value="2">Option 2</option>
                    <option value="3">Option 3</option>
                    <option value="4">Option 4</option>
                </select>
            </div>
        </div>
    `;
}

function resetQuizForm() {
    // Reset quiz type selection
    document.getElementById('quizType').value = '';
    document.getElementById('lessonSelection').style.display = 'none';
    document.getElementById('bookSelection').style.display = 'none';
    document.getElementById('quizLessonId').required = false;
    document.getElementById('quizBookId').required = false;
}

function updateLessonOptions(lessons) {
    const select = document.getElementById('quizLessonId');
    if (select) {
        select.innerHTML = '<option value="">Choose a lesson...</option>';
        
        lessons.forEach(lesson => {
            const option = document.createElement('option');
            option.value = lesson.id;
            option.textContent = lesson.title;
            select.appendChild(option);
        });
    }
}

function updateBookOptions(books) {
    const select = document.getElementById('quizBookId');
    if (select) {
        select.innerHTML = '<option value="">Choose a book...</option>';
        
        books.forEach(book => {
            const option = document.createElement('option');
            option.value = book.id;
            option.textContent = `${book.title} by ${book.author}`;
            select.appendChild(option);
        });
    }
}

function toggleQuizTarget() {
    const quizType = document.getElementById('quizType').value;
    const lessonSelection = document.getElementById('lessonSelection');
    const bookSelection = document.getElementById('bookSelection');
    
    if (quizType === 'lesson') {
        lessonSelection.style.display = 'block';
        bookSelection.style.display = 'none';
        document.getElementById('quizLessonId').required = true;
        document.getElementById('quizBookId').required = false;
    } else if (quizType === 'book') {
        lessonSelection.style.display = 'none';
        bookSelection.style.display = 'block';
        document.getElementById('quizLessonId').required = false;
        document.getElementById('quizBookId').required = true;
    } else {
        lessonSelection.style.display = 'none';
        bookSelection.style.display = 'none';
        document.getElementById('quizLessonId').required = false;
        document.getElementById('quizBookId').required = false;
    }
}

// Load lessons and books when quiz modal opens
async function loadQuizOptions() {
    try {
        const email = localStorage.getItem('userEmail');
        
        const [lessonsResponse, booksResponse] = await Promise.all([
            fetch(`http://localhost:8081/api/admin/lessons?email=${encodeURIComponent(email)}`),
            fetch(`http://localhost:8081/api/admin/books?email=${encodeURIComponent(email)}`)
        ]);
        
        if (lessonsResponse.ok) {
            const lessons = await lessonsResponse.json();
            updateLessonOptions(lessons);
        }
        
        if (booksResponse.ok) {
            const books = await booksResponse.json();
            updateBookOptions(books);
        }
    } catch (error) {
        console.error('Error loading quiz options:', error);
    }
}

// Utility Functions
function showMessage(text, type, messageId = null) {
    // If specific messageId provided, show in that element
    if (messageId) {
        const message = document.getElementById(messageId);
        if (message) {
            message.textContent = text;
            message.className = `form-message show ${type}`;
            setTimeout(() => {
                message.classList.remove('show');
            }, 3000);
        }
    } else {
        // Show as general notification (you could implement a toast system here)
        alert(text);
    }
}

// AI Quiz Generation
async function generateAIQuiz(bookId) {
    if (!confirm('Generate an AI quiz for this book? This may take a few moments.')) return;
    
    const email = localStorage.getItem('userEmail');
    
    try {
        showMessage('Generating AI quiz, please wait...', 'success');
        
        const response = await fetch(`http://localhost:8081/api/admin/generate-quiz/${bookId}?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showMessage(`AI quiz generated successfully! Created ${result.questionCount} questions.`, 'success');
            loadQuizzes(); // Refresh quiz list if we're on that section
            loadDashboardData(); // Update stats
        } else {
            showMessage(result.message || 'Failed to generate AI quiz', 'error');
        }
    } catch (error) {
        console.error('Generate AI quiz error:', error);
        showMessage('Error generating AI quiz', 'error');
    }
}

// System Management Functions

async function confirmClearAllContent() {
    // First confirmation - general warning
    const firstConfirm = confirm(
        '⚠️ WARNING: This will permanently delete ALL discovery learning content!\n\n' +
        'This includes:\n' +
        '• All tutor-uploaded content\n' +
        '• All uploaded files\n' +
        '• All related quizzes\n' +
        '• All student interactions\n' +
        '• All accessibility tags\n\n' +
        'This action CANNOT be undone!\n\n' +
        'Are you sure you want to continue?'
    );
    
    if (!firstConfirm) return;
    
    // Second confirmation - typing confirmation
    const confirmText = prompt(
        'To confirm this dangerous action, please type "DELETE ALL CONTENT" (without quotes) in the box below:'
    );
    
    if (confirmText !== 'DELETE ALL CONTENT') {
        alert('Confirmation text did not match. Operation cancelled.');
        return;
    }
    
    // Third confirmation - final warning
    const finalConfirm = confirm(
        'FINAL WARNING: You have confirmed that you want to delete ALL discovery learning content.\n\n' +
        'This will:\n' +
        '❌ Delete all tutor content from the database\n' +
        '❌ Delete all uploaded files from storage\n' +
        '❌ Delete all quizzes created by tutors\n' +
        '❌ Delete all student progress and interactions\n' +
        '❌ Delete all accessibility metadata\n\n' +
        'This action is IRREVERSIBLE!\n\n' +
        'Click OK to proceed with the deletion, or Cancel to abort.'
    );
    
    if (!finalConfirm) return;
    
    // Proceed with deletion
    await clearAllContent();
}

async function clearAllContent() {
    const email = localStorage.getItem('userEmail');
    const clearButton = document.getElementById('clearContentBtn');
    
    if (!email) {
        alert('Authentication error. Please log in again.');
        return;
    }
    
    try {
        // Disable button and show loading state
        clearButton.disabled = true;
        clearButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Clearing Content...';
        
        showMessage('Clearing all discovery learning content... This may take a few moments.', 'info');
        
        const response = await fetch(`http://localhost:8081/api/admin/clear-all-content?email=${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Show detailed success message
            const successMessage = `✅ Content cleared successfully!\n\n` +
                `📊 Summary:\n` +
                `• Content items deleted: ${result.deletedContent}\n` +
                `• Files deleted: ${result.deletedFiles}\n` +
                `• Quizzes deleted: ${result.deletedQuizzes}\n` +
                `• Interactions deleted: ${result.deletedInteractions}\n` +
                `• Tags deleted: ${result.deletedTags}`;
            
            showMessage(result.message, 'success');
            alert(successMessage);
            
            // Refresh dashboard data
            loadDashboardData();
            
        } else {
            showMessage(result.message || 'Failed to clear content', 'error');
            alert('❌ Error: ' + (result.message || 'Failed to clear content'));
        }
        
    } catch (error) {
        console.error('Clear content error:', error);
        showMessage('Error clearing content: ' + error.message, 'error');
        alert('❌ Error clearing content: ' + error.message);
    } finally {
        // Re-enable button
        clearButton.disabled = false;
        clearButton.innerHTML = '<i class="fas fa-trash-alt"></i> Clear All Discovery Content';
    }
}

function logout() {
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    window.location.href = '/index.html';
}