import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import LoadingSpinner from '../Common/LoadingSpinner';
import config from '../../services/config';
import axios from 'axios';
import './TeacherDashboard.css';

interface DashboardStats {
  totalStudents: number;
  studentsWithAssessments: number;
  studentsNeedingAccommodations: number;
  assessmentCompletionRate: number;
  accommodationRate: number;
  myStudentsCount?: number;
  assessmentsCompletedThisWeek?: number;
  accommodationsActive?: number;
}

interface StudentProfile {
  id: number;
  name: string;
  email: string;
  hasAssessment: boolean;
  recommendedPreset: string;
  primaryNeeds: string[];
  accommodationLevel: 'none' | 'minimal' | 'moderate' | 'comprehensive';
  uiPreset: string;
  hasCustomSettings: boolean;
  assessmentDate?: string;
}

interface ClassroomSummary {
  totalStudents: number;
  studentsWithAssessments: number;
  studentsNeedingAccommodations: number;
  assessmentCompletionRate: number;
  accommodationRate: number;
  presetDistribution: Record<string, number>;
  needsDistribution: Record<string, number>;
  classroomRecommendations: string[];
}

const TeacherDashboard: React.FC = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'overview' | 'students' | 'accessibility' | 'analytics'>('overview');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [students, setStudents] = useState<StudentProfile[]>([]);
  const [classroomSummary, setClassroomSummary] = useState<ClassroomSummary | null>(null);
  const [selectedStudent, setSelectedStudent] = useState<StudentProfile | null>(null);
  
  const API_BASE_URL = config.apiBaseUrl;

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      const [statsResponse, studentsResponse, classroomResponse] = await Promise.all([
        axios.get(`${API_BASE_URL}/api/teacher/dashboard/stats?teacherId=${user?.id}`, {
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }),
        axios.get(`${API_BASE_URL}/api/teacher/students?teacherId=${user?.id}`, {
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }),
        axios.get(`${API_BASE_URL}/api/teacher/classroom/accessibility?teacherId=${user?.id}`, {
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        })
      ]);
      
      setStats(statsResponse.data);
      setStudents(studentsResponse.data.students || []);
      setClassroomSummary(classroomResponse.data);
      
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
      showNotification('Failed to load dashboard data', 'error');
    } finally {
      setLoading(false);
    }
  };

  const getAccommodationColor = (level: string) => {
    switch (level) {
      case 'comprehensive': return '#dc3545';
      case 'moderate': return '#fd7e14';
      case 'minimal': return '#198754';
      default: return '#6c757d';
    }
  };

  const getPresetDisplayName = (preset: string) => {
    return preset.replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim() || 'Standard';
  };

  const handleStudentSelect = async (student: StudentProfile) => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/teacher/student/${student.id}/profile`,
        {
          headers: {
            'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
          },
        }
      );
      
      setSelectedStudent({...student, ...response.data});
    } catch (error) {
      console.error('Failed to fetch detailed student profile:', error);
      showNotification('Failed to load student details', 'error');
    }
  };

  if (loading) {
    return (
      <div className="teacher-dashboard">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="teacher-dashboard">
      <div className="dashboard-header">
        <h1>
          <i className="fas fa-chalkboard-teacher"></i>
          Teacher Dashboard
        </h1>
        <p>Manage student accommodations and track accessibility needs</p>
      </div>

      <div className="dashboard-tabs">
        <button 
          className={`tab-button ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          <i className="fas fa-chart-pie"></i>
          Overview
        </button>
        <button 
          className={`tab-button ${activeTab === 'students' ? 'active' : ''}`}
          onClick={() => setActiveTab('students')}
        >
          <i className="fas fa-users"></i>
          Students
        </button>
        <button 
          className={`tab-button ${activeTab === 'accessibility' ? 'active' : ''}`}
          onClick={() => setActiveTab('accessibility')}
        >
          <i className="fas fa-universal-access"></i>
          Classroom Summary
        </button>
        <button 
          className={`tab-button ${activeTab === 'analytics' ? 'active' : ''}`}
          onClick={() => setActiveTab('analytics')}
        >
          <i className="fas fa-chart-bar"></i>
          Analytics
        </button>
      </div>

      <div className="dashboard-content">
        {activeTab === 'overview' && stats && (
          <div className="overview-section">
            <div className="stats-grid">
              <div className="stat-card primary">
                <div className="stat-icon">
                  <i className="fas fa-users"></i>
                </div>
                <div className="stat-content">
                  <h3>{stats.totalStudents}</h3>
                  <p>Total Students</p>
                </div>
              </div>
              
              <div className="stat-card success">
                <div className="stat-icon">
                  <i className="fas fa-clipboard-check"></i>
                </div>
                <div className="stat-content">
                  <h3>{stats.studentsWithAssessments}</h3>
                  <p>Completed Assessments</p>
                </div>
              </div>
              
              <div className="stat-card warning">
                <div className="stat-icon">
                  <i className="fas fa-hands-helping"></i>
                </div>
                <div className="stat-content">
                  <h3>{stats.studentsNeedingAccommodations}</h3>
                  <p>Need Accommodations</p>
                </div>
              </div>
              
              <div className="stat-card info">
                <div className="stat-icon">
                  <i className="fas fa-percentage"></i>
                </div>
                <div className="stat-content">
                  <h3>{Math.round(stats.assessmentCompletionRate * 100)}%</h3>
                  <p>Assessment Rate</p>
                </div>
              </div>
            </div>

            <div className="quick-actions">
              <h3>Quick Actions</h3>
              <div className="actions-grid">
                <button 
                  className="action-button"
                  onClick={() => setActiveTab('students')}
                >
                  <i className="fas fa-user-graduate"></i>
                  View Student Profiles
                </button>
                <button 
                  className="action-button"
                  onClick={() => setActiveTab('accessibility')}
                >
                  <i className="fas fa-universal-access"></i>
                  Classroom Accommodations
                </button>
                <button className="action-button">
                  <i className="fas fa-file-export"></i>
                  Export Report
                </button>
                <button className="action-button">
                  <i className="fas fa-bell"></i>
                  Assessment Reminders
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'students' && (
          <div className="students-section">
            <div className="section-header">
              <h3>Student Accommodation Profiles</h3>
              <p>View and manage individual student accessibility needs</p>
            </div>
            
            <div className="students-grid">
              {students.map(student => (
                <div 
                  key={student.id} 
                  className="student-card"
                  onClick={() => handleStudentSelect(student)}
                >
                  <div className="student-header">
                    <div className="student-avatar">
                      <i className="fas fa-user"></i>
                    </div>
                    <div className="student-info">
                      <h4>{student.name}</h4>
                      <p>{student.email}</p>
                    </div>
                    <div 
                      className="accommodation-badge"
                      style={{ backgroundColor: getAccommodationColor(student.accommodationLevel) }}
                    >
                      {student.accommodationLevel}
                    </div>
                  </div>
                  
                  <div className="student-details">
                    <div className="detail-item">
                      <span className="label">Assessment:</span>
                      <span className={`value ${student.hasAssessment ? 'completed' : 'pending'}`}>
                        {student.hasAssessment ? 'Completed' : 'Pending'}
                      </span>
                    </div>
                    
                    <div className="detail-item">
                      <span className="label">UI Preset:</span>
                      <span className="value">{getPresetDisplayName(student.recommendedPreset)}</span>
                    </div>
                    
                    {student.primaryNeeds.length > 0 && (
                      <div className="primary-needs">
                        <span className="label">Primary Needs:</span>
                        <div className="needs-tags">
                          {student.primaryNeeds.map(need => (
                            <span key={need} className="need-tag">
                              {need}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'accessibility' && classroomSummary && (
          <div className="accessibility-section">
            <div className="section-header">
              <h3>Classroom Accessibility Summary</h3>
              <p>Overview of accommodation needs across your classroom</p>
            </div>
            
            <div className="summary-stats">
              <div className="summary-card">
                <h4>Assessment Completion</h4>
                <div className="progress-bar">
                  <div 
                    className="progress-fill"
                    style={{ width: `${classroomSummary.assessmentCompletionRate * 100}%` }}
                  ></div>
                </div>
                <p>{Math.round(classroomSummary.assessmentCompletionRate * 100)}% ({classroomSummary.studentsWithAssessments}/{classroomSummary.totalStudents})</p>
              </div>
              
              <div className="summary-card">
                <h4>Accommodation Rate</h4>
                <div className="progress-bar">
                  <div 
                    className="progress-fill warning"
                    style={{ width: `${classroomSummary.accommodationRate * 100}%` }}
                  ></div>
                </div>
                <p>{Math.round(classroomSummary.accommodationRate * 100)}% need accommodations</p>
              </div>
            </div>

            <div className="distribution-charts">
              <div className="chart-card">
                <h4>Common Accommodation Needs</h4>
                <div className="needs-breakdown">
                  {Object.entries(classroomSummary.needsDistribution).map(([need, count]) => (
                    <div key={need} className="need-breakdown-item">
                      <span className="need-name">{need}</span>
                      <div className="need-bar">
                        <div 
                          className="need-fill"
                          style={{ 
                            width: `${(count / classroomSummary.totalStudents) * 100}%`,
                            backgroundColor: need === 'attention' ? '#ff6b6b' : 
                                           need === 'reading' ? '#4ecdc4' :
                                           need === 'social' ? '#45b7d1' : '#96ceb4'
                          }}
                        ></div>
                      </div>
                      <span className="need-count">{count}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="chart-card">
                <h4>UI Preset Distribution</h4>
                <div className="preset-breakdown">
                  {Object.entries(classroomSummary.presetDistribution).map(([preset, count]) => (
                    <div key={preset} className="preset-item">
                      <span className="preset-name">{getPresetDisplayName(preset)}</span>
                      <span className="preset-count">{count} students</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="recommendations-card">
              <h4>Classroom Recommendations</h4>
              <div className="recommendations-list">
                {classroomSummary.classroomRecommendations.map((recommendation, index) => (
                  <div key={index} className="recommendation-item">
                    <i className="fas fa-lightbulb"></i>
                    <span>{recommendation}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'analytics' && (
          <div className="analytics-section">
            <div className="section-header">
              <h3>Analytics & Reports</h3>
              <p>Detailed insights into student accommodations and progress</p>
            </div>
            
            <div className="analytics-coming-soon">
              <i className="fas fa-chart-line"></i>
              <h4>Advanced Analytics Coming Soon</h4>
              <p>
                This section will include detailed reports on student progress, 
                accommodation effectiveness, and classroom trends.
              </p>
              <ul>
                <li>Student progress tracking over time</li>
                <li>Accommodation effectiveness metrics</li>
                <li>Comparative analysis with similar classrooms</li>
                <li>Detailed accessibility reports for administrators</li>
              </ul>
            </div>
          </div>
        )}
      </div>

      {/* Student Detail Modal */}
      {selectedStudent && (
        <div className="modal-overlay" onClick={() => setSelectedStudent(null)}>
          <div className="student-detail-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{selectedStudent.name} - Accommodation Profile</h3>
              <button 
                className="close-button"
                onClick={() => setSelectedStudent(null)}
              >
                <i className="fas fa-times"></i>
              </button>
            </div>
            
            <div className="modal-content">
              <div className="profile-section">
                <h4>Assessment Results</h4>
                {selectedStudent.hasAssessment ? (
                  <div className="assessment-results">
                    <p><strong>Completed:</strong> {selectedStudent.assessmentDate}</p>
                    <p><strong>Recommended Preset:</strong> {getPresetDisplayName(selectedStudent.recommendedPreset)}</p>
                    <p><strong>Accommodation Level:</strong> {selectedStudent.accommodationLevel}</p>
                  </div>
                ) : (
                  <p className="no-assessment">Assessment not completed yet.</p>
                )}
              </div>
              
              {selectedStudent.primaryNeeds.length > 0 && (
                <div className="profile-section">
                  <h4>Primary Accommodation Needs</h4>
                  <div className="needs-list">
                    {selectedStudent.primaryNeeds.map(need => (
                      <span key={need} className="need-badge">
                        {need}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TeacherDashboard;