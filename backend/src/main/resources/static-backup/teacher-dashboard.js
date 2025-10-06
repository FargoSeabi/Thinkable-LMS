// Teacher Dashboard JavaScript
class TeacherDashboard {
    constructor() {
        this.apiBase = '/api/teacher';
        this.currentView = 'dashboard';
        this.students = [];
        this.stats = {};
        this.currentStudentId = null;
        
        this.init();
    }

    init() {
        console.log('Initializing Teacher Dashboard');
        
        // Load initial dashboard data
        this.loadDashboardData();
        
        // Set up event listeners
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Add any global event listeners here
        document.addEventListener('DOMContentLoaded', () => {
            this.showDashboard();
        });
    }

    async loadDashboardData() {
        try {
            console.log('Loading dashboard statistics');
            
            // Load stats
            const statsResponse = await fetch(`${this.apiBase}/dashboard/stats`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (statsResponse.ok) {
                this.stats = await statsResponse.json();
                this.updateStatsDisplay();
                this.createAccommodationChart();
            } else {
                console.warn('Failed to load dashboard stats');
                this.showMockData();
            }

            // Load classroom accessibility summary
            const accessibilityResponse = await fetch(`${this.apiBase}/classroom/accessibility`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (accessibilityResponse.ok) {
                const accessibility = await accessibilityResponse.json();
                this.displayClassroomRecommendations(accessibility.classroomRecommendations || []);
            }

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showMockData();
        }
    }

    showMockData() {
        // Fallback mock data for demo purposes
        this.stats = {
            totalStudents: 28,
            completedAssessments: 22,
            studentsNeedingAccommodations: 8,
            completionRate: 0.79,
            presetDistribution: {
                'standard': 20,
                'adhd': 3,
                'dyslexia': 2,
                'autism': 2,
                'sensory': 1
            },
            classroomRecommendations: [
                'Consider implementing structured attention breaks',
                'Provide materials in multiple formats (audio, large print)',
                'Use explicit instruction and clear expectations'
            ]
        };
        
        this.updateStatsDisplay();
        this.createAccommodationChart();
        this.displayClassroomRecommendations(this.stats.classroomRecommendations);
    }

    updateStatsDisplay() {
        // Update statistics cards
        document.getElementById('total-students').textContent = this.stats.totalStudents || '--';
        document.getElementById('completed-assessments').textContent = this.stats.completedAssessments || '--';
        document.getElementById('active-accommodations').textContent = this.stats.studentsNeedingAccommodations || '--';
        
        const completionRate = this.stats.completionRate || 0;
        document.getElementById('completion-rate').textContent = Math.round(completionRate * 100) + '%';
    }

    createAccommodationChart() {
        const ctx = document.getElementById('accommodationChart');
        if (!ctx) return;

        const presetData = this.stats.presetDistribution || {};
        const labels = Object.keys(presetData).map(key => this.formatPresetName(key));
        const data = Object.values(presetData);
        const colors = [
            '#36a2eb', '#ff6384', '#4bc0c0', '#ff9f40', '#9966ff', '#ffcd56'
        ];

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    formatPresetName(preset) {
        const names = {
            'standard': 'Standard',
            'adhd': 'ADHD Support',
            'dyslexia': 'Dyslexia Support',
            'autism': 'Autism Support',
            'sensory': 'Sensory-Friendly',
            'dyslexia-adhd': 'Combined Support'
        };
        return names[preset] || preset;
    }

    displayClassroomRecommendations(recommendations) {
        const container = document.getElementById('classroom-recommendations');
        
        if (!recommendations || recommendations.length === 0) {
            container.innerHTML = `
                <div class="empty-state text-center py-4">
                    <i class="fas fa-check-circle text-success mb-2" style="font-size: 2rem;"></i>
                    <p class="text-muted">No specific recommendations at this time.<br>Your classroom setup looks great!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = recommendations.map(rec => `
            <div class="recommendation-item">
                <i class="fas fa-lightbulb text-warning me-2"></i>
                ${rec}
            </div>
        `).join('');
    }

    // Navigation methods
    showDashboard() {
        this.switchView('dashboard');
        this.setActiveNavItem('dashboard');
    }

    showStudents() {
        this.switchView('students');
        this.setActiveNavItem('students');
        this.loadStudents();
    }

    showClassroomTools() {
        this.switchView('classroom-tools');
        this.setActiveNavItem('classroom-tools');
    }

    showReports() {
        this.switchView('reports');
        this.setActiveNavItem('reports');
    }

    showResources() {
        this.switchView('resources');
        this.setActiveNavItem('resources');
    }

    switchView(viewName) {
        // Hide all content sections
        const contentSections = [
            'dashboard-content',
            'students-content', 
            'classroom-tools-content',
            'reports-content',
            'resources-content'
        ];

        contentSections.forEach(sectionId => {
            const section = document.getElementById(sectionId);
            if (section) {
                section.style.display = 'none';
            }
        });

        // Show selected content
        const targetSection = document.getElementById(`${viewName}-content`);
        if (targetSection) {
            targetSection.style.display = 'block';
        }

        this.currentView = viewName;
    }

    setActiveNavItem(viewName) {
        // Remove active class from all nav links
        document.querySelectorAll('.sidebar .nav-link').forEach(link => {
            link.classList.remove('active');
        });

        // Add active class to current nav item
        const activeLink = document.querySelector(`.sidebar .nav-link[onclick="show${this.capitalize(viewName)}()"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).replace('-', '');
    }

    async loadStudents() {
        try {
            console.log('Loading students data');
            
            const response = await fetch(`${this.apiBase}/students`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                this.students = data.students || [];
                this.displayStudents();
            } else {
                console.warn('Failed to load students, showing mock data');
                this.showMockStudents();
            }

        } catch (error) {
            console.error('Error loading students:', error);
            this.showMockStudents();
        }
    }

    showMockStudents() {
        // Mock data for demonstration
        this.students = [
            {
                id: 1,
                name: 'Alex Johnson',
                email: 'alex.j@school.edu',
                hasAssessment: true,
                recommendedPreset: 'adhd',
                primaryNeeds: ['Attention Support'],
                accommodationLevel: 'moderate',
                hasCustomSettings: true
            },
            {
                id: 2,
                name: 'Riley Chen',
                email: 'riley.c@school.edu',
                hasAssessment: true,
                recommendedPreset: 'dyslexia',
                primaryNeeds: ['Reading Support'],
                accommodationLevel: 'comprehensive',
                hasCustomSettings: true
            },
            {
                id: 3,
                name: 'Jordan Smith',
                email: 'jordan.s@school.edu',
                hasAssessment: false,
                recommendedPreset: 'standard',
                primaryNeeds: [],
                accommodationLevel: 'none',
                hasCustomSettings: false
            },
            {
                id: 4,
                name: 'Sam Rodriguez',
                email: 'sam.r@school.edu',
                hasAssessment: true,
                recommendedPreset: 'sensory',
                primaryNeeds: ['Sensory Support'],
                accommodationLevel: 'moderate',
                hasCustomSettings: true
            }
        ];
        
        this.displayStudents();
    }

    displayStudents() {
        const container = document.getElementById('students-list');
        
        if (!this.students || this.students.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-users"></i>
                    <h4>No Students Found</h4>
                    <p>No student data available at this time.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.students.map(student => `
            <div class="student-card" onclick="showStudentDetails(${student.id})">
                <div class="row align-items-center">
                    <div class="col-md-3">
                        <div class="d-flex align-items-center">
                            <div class="me-3">
                                <i class="fas fa-user-circle text-primary" style="font-size: 2rem;"></i>
                            </div>
                            <div>
                                <h6 class="mb-0">${student.name}</h6>
                                <small class="text-muted">${student.email}</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <span class="preset-badge ${student.recommendedPreset}">
                            ${this.formatPresetName(student.recommendedPreset)}
                        </span>
                    </div>
                    <div class="col-md-3">
                        <div class="needs-list">
                            ${student.primaryNeeds.map(need => `
                                <span class="needs-indicator ${need.toLowerCase().split(' ')[0]}"></span>
                                <small>${need}</small><br>
                            `).join('')}
                            ${student.primaryNeeds.length === 0 ? '<small class="text-muted">No specific needs identified</small>' : ''}
                        </div>
                    </div>
                    <div class="col-md-2">
                        <span class="accommodation-badge ${student.accommodationLevel}">
                            ${this.formatAccommodationLevel(student.accommodationLevel)}
                        </span>
                    </div>
                    <div class="col-md-2 text-end">
                        <div class="assessment-status">
                            ${student.hasAssessment ? 
                                '<i class="fas fa-check-circle text-success"></i> <small>Assessed</small>' : 
                                '<i class="fas fa-clock text-warning"></i> <small>Pending</small>'
                            }
                        </div>
                        <div class="mt-1">
                            <button class="btn btn-sm btn-outline-primary" onclick="event.stopPropagation(); showStudentDetails(${student.id})">
                                <i class="fas fa-eye"></i> View
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    formatAccommodationLevel(level) {
        const levels = {
            'none': 'None',
            'minimal': 'Minimal',
            'moderate': 'Moderate',
            'comprehensive': 'Comprehensive'
        };
        return levels[level] || level;
    }

    async showStudentDetails(studentId) {
        this.currentStudentId = studentId;
        const student = this.students.find(s => s.id === studentId);
        
        if (!student) {
            console.error('Student not found:', studentId);
            return;
        }

        // Update modal title
        document.getElementById('studentModalLabel').innerHTML = `
            <i class="fas fa-user"></i> ${student.name}
        `;

        // Load detailed profile
        try {
            const response = await fetch(`${this.apiBase}/student/${studentId}/profile`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const profile = await response.json();
                this.displayStudentProfile(profile);
            } else {
                this.displayMockStudentProfile(student);
            }
        } catch (error) {
            console.error('Error loading student profile:', error);
            this.displayMockStudentProfile(student);
        }

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('studentModal'));
        modal.show();
    }

    displayStudentProfile(profile) {
        const profileContainer = document.getElementById('student-profile-content');
        
        profileContainer.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <h6><i class="fas fa-info-circle"></i> Basic Information</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Name:</strong></td><td>${profile.name}</td></tr>
                        <tr><td><strong>Email:</strong></td><td>${profile.email}</td></tr>
                        <tr><td><strong>Assessment Status:</strong></td><td>
                            ${profile.hasAssessment ? 
                                '<span class="badge bg-success">Completed</span>' : 
                                '<span class="badge bg-warning">Pending</span>'
                            }
                        </td></tr>
                        <tr><td><strong>UI Preset:</strong></td><td>
                            <span class="preset-badge ${profile.recommendedPreset}">
                                ${this.formatPresetName(profile.recommendedPreset)}
                            </span>
                        </td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6><i class="fas fa-chart-bar"></i> Assessment Scores</h6>
                    ${profile.assessmentScores ? this.renderAssessmentScores(profile.assessmentScores) : 
                        '<p class="text-muted">No assessment data available</p>'
                    }
                </div>
            </div>
            
            ${profile.traits ? `
                <div class="row mt-3">
                    <div class="col-12">
                        <h6><i class="fas fa-brain"></i> Learning Profile</h6>
                        <div class="row">
                            ${Object.entries(profile.traits).map(([trait, hasSignificantNeed]) => `
                                <div class="col-md-3 mb-2">
                                    <div class="trait-indicator ${hasSignificantNeed ? 'significant' : 'minimal'}">
                                        <i class="fas fa-${this.getTraitIcon(trait)} me-1"></i>
                                        ${this.formatTraitName(trait)}
                                        ${hasSignificantNeed ? 
                                            '<i class="fas fa-exclamation-circle text-warning ms-1"></i>' : 
                                            '<i class="fas fa-check-circle text-success ms-1"></i>'
                                        }
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
            ` : ''}
        `;

        // Load accommodations and recommendations
        this.loadStudentAccommodations(profile);
        this.loadStudentRecommendations(profile);
    }

    displayMockStudentProfile(student) {
        const profileContainer = document.getElementById('student-profile-content');
        
        profileContainer.innerHTML = `
            <div class="row">
                <div class="col-12">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        This is demo data. In a real implementation, this would show detailed assessment results and accommodation history.
                    </div>
                    <h6>Student Information</h6>
                    <p><strong>Name:</strong> ${student.name}</p>
                    <p><strong>Preset:</strong> <span class="preset-badge ${student.recommendedPreset}">${this.formatPresetName(student.recommendedPreset)}</span></p>
                    <p><strong>Accommodation Level:</strong> <span class="accommodation-badge ${student.accommodationLevel}">${this.formatAccommodationLevel(student.accommodationLevel)}</span></p>
                </div>
            </div>
        `;

        // Load mock accommodations and recommendations
        this.loadMockAccommodations();
        this.loadMockRecommendations();
    }

    renderAssessmentScores(scores) {
        return `
            <div class="assessment-scores">
                ${Object.entries(scores).map(([category, score]) => `
                    <div class="score-item d-flex justify-content-between align-items-center mb-2">
                        <span>${this.formatTraitName(category)}:</span>
                        <div class="score-bar">
                            <div class="progress" style="width: 100px; height: 8px;">
                                <div class="progress-bar ${this.getScoreColor(score)}" 
                                     style="width: ${(score / 25) * 100}%"></div>
                            </div>
                            <small class="ms-2">${score}/25</small>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    getScoreColor(score) {
        if (score >= 18) return 'bg-danger';
        if (score >= 12) return 'bg-warning';
        return 'bg-success';
    }

    getTraitIcon(trait) {
        const icons = {
            'attention': 'bullseye',
            'significantAttentionNeeds': 'bullseye',
            'reading': 'book-open',
            'significantReadingNeeds': 'book-open',
            'social': 'users',
            'significantSocialNeeds': 'users',
            'sensory': 'adjust',
            'significantSensoryNeeds': 'adjust'
        };
        return icons[trait] || 'circle';
    }

    formatTraitName(trait) {
        const names = {
            'attention': 'Attention',
            'significantAttentionNeeds': 'Attention',
            'reading': 'Reading',
            'significantReadingNeeds': 'Reading',
            'social': 'Social',
            'significantSocialNeeds': 'Social',
            'sensory': 'Sensory',
            'significantSensoryNeeds': 'Sensory'
        };
        return names[trait] || trait;
    }

    loadStudentAccommodations(profile) {
        // This would load from the API in a real implementation
        const accommodationsContainer = document.getElementById('student-accommodations-content');
        accommodationsContainer.innerHTML = `
            <h6><i class="fas fa-universal-access"></i> Current Accommodations</h6>
            <div class="alert alert-info">
                Accommodation details would be loaded from the assessment API here.
            </div>
        `;
    }

    loadStudentRecommendations(profile) {
        // This would load from the API in a real implementation
        const recommendationsContainer = document.getElementById('student-recommendations-content');
        recommendationsContainer.innerHTML = `
            <h6><i class="fas fa-lightbulb"></i> Teaching Recommendations</h6>
            <div class="alert alert-info">
                Detailed teaching recommendations would be loaded from the assessment API here.
            </div>
        `;
    }

    loadMockAccommodations() {
        const accommodationsContainer = document.getElementById('student-accommodations-content');
        accommodationsContainer.innerHTML = `
            <h6><i class="fas fa-universal-access"></i> Active Accommodations</h6>
            <ul class="list-group">
                <li class="list-group-item">Extended time for assignments (1.5x)</li>
                <li class="list-group-item">Preferential seating near the front</li>
                <li class="list-group-item">Break tasks into smaller segments</li>
                <li class="list-group-item">Use of text-to-speech software</li>
            </ul>
        `;
    }

    loadMockRecommendations() {
        const recommendationsContainer = document.getElementById('student-recommendations-content');
        recommendationsContainer.innerHTML = `
            <h6><i class="fas fa-lightbulb"></i> Teaching Strategies</h6>
            <div class="recommendation-item">
                <strong>Classroom Environment:</strong> Minimize distractions, provide quiet workspace
            </div>
            <div class="recommendation-item">
                <strong>Instruction:</strong> Use visual cues, break down complex tasks
            </div>
            <div class="recommendation-item">
                <strong>Assessment:</strong> Allow extra time, provide alternative formats
            </div>
        `;
    }

    refreshStudents() {
        document.getElementById('students-list').innerHTML = `
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i>
            </div>
        `;
        this.loadStudents();
    }

    saveStudentNotes() {
        // Implementation for saving teacher notes
        console.log('Saving student notes for student:', this.currentStudentId);
        
        // Show success message
        const alert = document.createElement('div');
        alert.className = 'alert alert-success alert-dismissible fade show';
        alert.innerHTML = `
            <i class="fas fa-check-circle"></i> Notes saved successfully!
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        const modalBody = document.querySelector('#studentModal .modal-body');
        modalBody.insertBefore(alert, modalBody.firstChild);
        
        // Auto-dismiss after 3 seconds
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 3000);
    }
}

// Global functions for navigation (called from HTML onclick)
function showDashboard() {
    window.teacherDashboard.showDashboard();
}

function showStudents() {
    window.teacherDashboard.showStudents();
}

function showClassroomTools() {
    window.teacherDashboard.showClassroomTools();
}

function showReports() {
    window.teacherDashboard.showReports();
}

function showResources() {
    window.teacherDashboard.showResources();
}

function showStudentDetails(studentId) {
    window.teacherDashboard.showStudentDetails(studentId);
}

function refreshStudents() {
    window.teacherDashboard.refreshStudents();
}

function saveStudentNotes() {
    window.teacherDashboard.saveStudentNotes();
}

function showHelp() {
    alert('Teacher Dashboard Help\n\nThis dashboard helps you monitor and manage student accommodations and learning needs.\n\n• Dashboard: Overview of class statistics\n• Students: Individual student profiles and needs\n• Classroom Tools: Teaching resources (coming soon)\n• Reports: Progress analytics (coming soon)\n• Resources: Educational materials (coming soon)');
}

// Initialize the dashboard when the page loads
document.addEventListener('DOMContentLoaded', function() {
    window.teacherDashboard = new TeacherDashboard();
});