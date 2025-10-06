// Custom Modal System for ThinkAble
// This replaces browser alert() and confirm() with user-friendly modals

// Initialize modal system when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add modal HTML to body if it doesn't exist
    if (!document.getElementById('customModal')) {
        const modalHTML = `
            <div id="customModal" class="custom-modal">
                <div class="custom-modal-content">
                    <div class="modal-header">
                        <div id="modalIcon" class="modal-icon">
                            <i id="modalIconSymbol" class="fas fa-info-circle"></i>
                        </div>
                        <h5 id="modalTitle" class="modal-title">Notification</h5>
                    </div>
                    <div id="modalBody" class="modal-body">
                        <!-- Message content will be inserted here -->
                    </div>
                    <div id="modalFooter" class="modal-footer">
                        <!-- Buttons will be inserted here -->
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        // Add modal styles if they don't exist
        if (!document.getElementById('customModalStyles')) {
            const styles = `
                <style id="customModalStyles">
                    .custom-modal {
                        display: none;
                        position: fixed;
                        z-index: 1050;
                        left: 0;
                        top: 0;
                        width: 100%;
                        height: 100%;
                        background-color: rgba(0, 0, 0, 0.5);
                        backdrop-filter: blur(2px);
                    }

                    .custom-modal.show {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }

                    .custom-modal-content {
                        background: white;
                        border-radius: 15px;
                        padding: 2rem;
                        max-width: 500px;
                        width: 90%;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                        transform: scale(0.7);
                        opacity: 0;
                        transition: all 0.3s ease;
                    }

                    .custom-modal.show .custom-modal-content {
                        transform: scale(1);
                        opacity: 1;
                    }

                    .modal-header {
                        display: flex;
                        align-items: center;
                        margin-bottom: 1rem;
                    }

                    .modal-header .modal-icon {
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin-right: 1rem;
                        font-size: 1.2rem;
                    }

                    .modal-header .modal-icon.success {
                        background-color: #d4edda;
                        color: #155724;
                    }

                    .modal-header .modal-icon.warning {
                        background-color: #fff3cd;
                        color: #856404;
                    }

                    .modal-header .modal-icon.danger {
                        background-color: #f8d7da;
                        color: #721c24;
                    }

                    .modal-header .modal-icon.info {
                        background-color: #d1ecf1;
                        color: #0c5460;
                    }

                    .modal-title {
                        font-size: 1.25rem;
                        font-weight: 600;
                        margin: 0;
                    }

                    .modal-body {
                        margin-bottom: 1.5rem;
                        line-height: 1.5;
                        color: #495057;
                    }

                    .modal-footer {
                        display: flex;
                        gap: 0.75rem;
                        justify-content: flex-end;
                    }

                    .modal-btn {
                        padding: 0.5rem 1.5rem;
                        border: none;
                        border-radius: 8px;
                        font-weight: 500;
                        cursor: pointer;
                        transition: all 0.2s ease;
                    }

                    .modal-btn.primary {
                        background-color: #2c5aa0;
                        color: white;
                    }

                    .modal-btn.primary:hover {
                        background-color: #5a7cb8;
                    }

                    .modal-btn.danger {
                        background-color: #dc3545;
                        color: white;
                    }

                    .modal-btn.danger:hover {
                        background-color: #c82333;
                    }

                    .modal-btn.secondary {
                        background-color: #6c757d;
                        color: white;
                    }

                    .modal-btn.secondary:hover {
                        background-color: #5a6268;
                    }

                    .modal-btn.success {
                        background-color: #28a745;
                        color: white;
                    }

                    .modal-btn.success:hover {
                        background-color: #218838;
                    }
                </style>
            `;
            document.head.insertAdjacentHTML('beforeend', styles);
        }
    }
});

function showCustomModal(type, title, message, buttons) {
    const modal = document.getElementById('customModal');
    const modalIcon = document.getElementById('modalIcon');
    const modalIconSymbol = document.getElementById('modalIconSymbol');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const modalFooter = document.getElementById('modalFooter');

    // Set icon and style based on type
    modalIcon.className = `modal-icon ${type}`;
    switch(type) {
        case 'success':
            modalIconSymbol.className = 'fas fa-check-circle';
            break;
        case 'warning':
            modalIconSymbol.className = 'fas fa-exclamation-triangle';
            break;
        case 'danger':
            modalIconSymbol.className = 'fas fa-times-circle';
            break;
        case 'info':
        default:
            modalIconSymbol.className = 'fas fa-info-circle';
            break;
    }

    modalTitle.textContent = title;
    modalBody.innerHTML = message;
    
    // Clear and add buttons
    modalFooter.innerHTML = '';
    buttons.forEach(button => {
        const btn = document.createElement('button');
        btn.className = `modal-btn ${button.class || 'secondary'}`;
        btn.textContent = button.text;
        btn.onclick = () => {
            hideCustomModal();
            if (button.action && typeof button.action === 'function') {
                button.action();
            } else if (button.action) {
                console.error('Button action is not a function:', typeof button.action, button.action);
            }
        };
        modalFooter.appendChild(btn);
    });

    modal.classList.add('show');
    
    // Close modal when clicking outside
    modal.onclick = (e) => {
        if (e.target === modal) hideCustomModal();
    };
}

function hideCustomModal() {
    const modal = document.getElementById('customModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

// Custom alert function
function customAlert(message, type = 'info', title = 'Notification') {
    showCustomModal(type, title, message, [
        { text: 'OK', class: 'primary' }
    ]);
}

// Custom confirm function - supports both callback and Promise patterns
function customConfirm(message, onConfirmOrTitle, typeOrCallback, titleOrType) {
    // Handle different parameter patterns
    let onConfirm, type, title;
    
    if (typeof onConfirmOrTitle === 'function') {
        // Pattern: customConfirm(message, callback, type, title)
        onConfirm = onConfirmOrTitle;
        type = typeOrCallback || 'warning';
        title = titleOrType || 'Confirm Action';
    } else if (typeof typeOrCallback === 'function') {
        // Pattern: customConfirm(message, title, callback, type)  
        title = onConfirmOrTitle || 'Confirm Action';
        onConfirm = typeOrCallback;
        type = titleOrType || 'warning';
    } else {
        // Pattern: customConfirm(message, title) - Promise pattern
        title = onConfirmOrTitle || 'Confirm Action';
        type = typeOrCallback || 'warning';
        
        // Return a Promise for async/await usage
        return new Promise((resolve) => {
            showCustomModal(type, title, message, [
                { text: 'Cancel', class: 'secondary', action: () => resolve(false) },
                { text: 'Confirm', class: type === 'danger' ? 'danger' : 'primary', action: () => resolve(true) }
            ]);
        });
    }
    
    // Traditional callback pattern
    showCustomModal(type, title, message, [
        { text: 'Cancel', class: 'secondary' },
        { text: 'Confirm', class: type === 'danger' ? 'danger' : 'primary', action: onConfirm }
    ]);
}

// Override browser alert and confirm with custom versions
window.originalAlert = window.alert;
window.originalConfirm = window.confirm;

window.alert = function(message) {
    customAlert(message);
};

window.confirm = function(message) {
    console.warn('confirm() called but no callback provided. Use customConfirm() instead.');
    return false; // Default to cancel
};

// Export functions for explicit use
window.customAlert = customAlert;
window.customConfirm = customConfirm;
window.showCustomModal = showCustomModal;
window.hideCustomModal = hideCustomModal;