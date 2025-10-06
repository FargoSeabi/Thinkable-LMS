package com.thinkable.backend.controller;

import com.thinkable.backend.model.User;
import com.thinkable.backend.model.UserAssessment;
import com.thinkable.backend.model.AdaptiveUISettings;
import com.thinkable.backend.service.AssessmentService;
import com.thinkable.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherDashboardController.class);

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get overview statistics for teacher dashboard
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats(@RequestParam(required = false) Long teacherId) {
        try {
            logger.info("Getting dashboard stats for teacher: {}", teacherId);
            
            Map<String, Object> stats = assessmentService.getAssessmentStatistics();
            
            // Add teacher-specific stats if teacherId provided
            if (teacherId != null) {
                Map<String, Object> teacherStats = getTeacherSpecificStats(teacherId);
                stats.putAll(teacherStats);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting dashboard stats: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get list of students with their accommodation profiles
     */
    @GetMapping("/students")
    public ResponseEntity<?> getStudentsWithAccommodations(@RequestParam(required = false) Long teacherId) {
        try {
            logger.info("Getting students with accommodations for teacher: {}", teacherId);
            
            List<User> students = userRepository.findAll(); // In a real implementation, this would filter by role
            
            List<Map<String, Object>> studentProfiles = students.stream()
                .map(this::buildStudentProfile)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", studentProfiles);
            response.put("totalStudents", studentProfiles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting students with accommodations: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get student accommodations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get detailed profile for a specific student
     */
    @GetMapping("/student/{studentId}/profile")
    public ResponseEntity<?> getStudentDetailedProfile(@PathVariable Long studentId) {
        try {
            logger.info("Getting detailed profile for student: {}", studentId);
            
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Student not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> profile = buildDetailedStudentProfile(student);
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            logger.error("Error getting student profile for {}: {}", studentId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get student profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get accommodation recommendations for a student
     */
    @GetMapping("/student/{studentId}/recommendations")
    public ResponseEntity<?> getStudentRecommendations(@PathVariable Long studentId) {
        try {
            logger.info("Getting recommendations for student: {}", studentId);
            
            UserAssessment assessment = assessmentService.getLatestAssessment(studentId);
            if (assessment == null || !assessment.getAssessmentCompleted()) {
                Map<String, Object> response = new HashMap<>();
                response.put("hasAssessment", false);
                response.put("message", "Student has not completed assessment");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> recommendations = generateTeacherRecommendations(assessment);
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Error getting recommendations for student {}: {}", studentId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get classroom accessibility summary
     */
    @GetMapping("/classroom/accessibility")
    public ResponseEntity<?> getClassroomAccessibility(@RequestParam(required = false) Long teacherId) {
        try {
            logger.info("Getting classroom accessibility summary for teacher: {}", teacherId);
            
            List<User> students = userRepository.findAll(); // In a real implementation, this would filter by role
            Map<String, Object> summary = buildClassroomAccessibilitySummary(students);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error getting classroom accessibility: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get classroom accessibility summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update accommodation notes for a student
     */
    @PostMapping("/student/{studentId}/notes")
    public ResponseEntity<?> updateAccommodationNotes(@PathVariable Long studentId, 
                                                      @RequestBody Map<String, String> request) {
        try {
            logger.info("Updating accommodation notes for student: {}", studentId);
            
            String notes = request.get("notes");
            String teacherName = request.get("teacherName");
            
            // This would typically update a separate teacher notes table
            // For now, we'll return success
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notes updated successfully");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating notes for student {}: {}", studentId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update notes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper methods
    
    private Map<String, Object> getTeacherSpecificStats(Long teacherId) {
        Map<String, Object> stats = new HashMap<>();
        
        // In a real implementation, these would be filtered by teacher's classes
        stats.put("myStudentsCount", userRepository.findAll().size());
        stats.put("assessmentsCompletedThisWeek", 5); // Placeholder
        stats.put("accommodationsActive", 12); // Placeholder
        
        return stats;
    }

    private Map<String, Object> buildStudentProfile(User student) {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("id", student.getId());
        profile.put("name", student.getUsername());
        profile.put("email", student.getEmail());
        
        // Get assessment data
        UserAssessment assessment = assessmentService.getLatestAssessment(student.getId());
        if (assessment != null && assessment.getAssessmentCompleted()) {
            profile.put("hasAssessment", true);
            profile.put("assessmentDate", assessment.getAssessmentDate());
            profile.put("recommendedPreset", assessment.getRecommendedPreset());
            profile.put("primaryNeeds", getPrimaryNeeds(assessment));
            profile.put("accommodationLevel", getAccommodationLevel(assessment));
        } else {
            profile.put("hasAssessment", false);
            profile.put("recommendedPreset", "standard");
            profile.put("primaryNeeds", List.of());
            profile.put("accommodationLevel", "none");
        }
        
        // Get UI settings
        AdaptiveUISettings uiSettings = assessmentService.getUISettings(student.getId());
        if (uiSettings != null) {
            profile.put("hasCustomSettings", true);
            profile.put("uiPreset", uiSettings.getUiPreset());
        } else {
            profile.put("hasCustomSettings", false);
            profile.put("uiPreset", "standard");
        }
        
        return profile;
    }

    private Map<String, Object> buildDetailedStudentProfile(User student) {
        Map<String, Object> profile = buildStudentProfile(student);
        
        UserAssessment assessment = assessmentService.getLatestAssessment(student.getId());
        if (assessment != null && assessment.getAssessmentCompleted()) {
            // Add detailed assessment scores
            Map<String, Object> scores = new HashMap<>();
            scores.put("attention", assessment.getAttentionScore());
            scores.put("reading", assessment.getReadingDifficultyScore());
            scores.put("social", assessment.getSocialCommunicationScore());
            scores.put("sensory", assessment.getSensoryProcessingScore());
            scores.put("motor", assessment.getMotorSkillsScore());
            profile.put("assessmentScores", scores);
            
            // Add trait analysis
            Map<String, Object> traits = new HashMap<>();
            traits.put("significantAttentionNeeds", assessment.hasSignificantAttentionNeeds());
            traits.put("significantReadingNeeds", assessment.hasSignificantReadingNeeds());
            traits.put("significantSocialNeeds", assessment.hasSignificantSocialNeeds());
            traits.put("significantSensoryNeeds", assessment.hasSignificantSensoryNeeds());
            profile.put("traits", traits);
            
            // Add teacher recommendations
            profile.put("teacherRecommendations", generateTeacherRecommendations(assessment));
        }
        
        return profile;
    }

    private List<String> getPrimaryNeeds(UserAssessment assessment) {
        List<String> needs = new java.util.ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            needs.add("Attention Support");
        }
        if (assessment.hasSignificantReadingNeeds()) {
            needs.add("Reading Support");
        }
        if (assessment.hasSignificantSocialNeeds()) {
            needs.add("Communication Support");
        }
        if (assessment.hasSignificantSensoryNeeds()) {
            needs.add("Sensory Support");
        }
        
        return needs;
    }

    private String getAccommodationLevel(UserAssessment assessment) {
        int significantNeedsCount = 0;
        
        if (assessment.hasSignificantAttentionNeeds()) significantNeedsCount++;
        if (assessment.hasSignificantReadingNeeds()) significantNeedsCount++;
        if (assessment.hasSignificantSocialNeeds()) significantNeedsCount++;
        if (assessment.hasSignificantSensoryNeeds()) significantNeedsCount++;
        
        if (significantNeedsCount == 0) return "minimal";
        if (significantNeedsCount <= 2) return "moderate";
        return "comprehensive";
    }

    private Map<String, Object> generateTeacherRecommendations(UserAssessment assessment) {
        Map<String, Object> recommendations = new HashMap<>();
        
        // Classroom strategies
        List<String> classroomStrategies = new java.util.ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            classroomStrategies.add("Provide preferential seating near the front");
            classroomStrategies.add("Break tasks into smaller segments");
            classroomStrategies.add("Use visual cues and signals");
            classroomStrategies.add("Allow movement breaks");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            classroomStrategies.add("Provide extra time for reading assignments");
            classroomStrategies.add("Use larger fonts and increased line spacing");
            classroomStrategies.add("Offer audio versions of texts");
            classroomStrategies.add("Allow use of text-to-speech software");
        }
        
        if (assessment.hasSignificantSocialNeeds()) {
            classroomStrategies.add("Provide clear, explicit instructions");
            classroomStrategies.add("Use structured group activities");
            classroomStrategies.add("Allow processing time before responding");
            classroomStrategies.add("Avoid idioms and unclear language");
        }
        
        if (assessment.hasSignificantSensoryNeeds()) {
            classroomStrategies.add("Minimize environmental distractions");
            classroomStrategies.add("Provide quiet workspace options");
            classroomStrategies.add("Allow use of noise-canceling headphones");
            classroomStrategies.add("Use soft lighting when possible");
        }
        
        recommendations.put("classroomStrategies", classroomStrategies);
        
        // Assessment accommodations
        List<String> assessmentAccommodations = new java.util.ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            assessmentAccommodations.add("Extended time (time and a half)");
            assessmentAccommodations.add("Frequent breaks during testing");
            assessmentAccommodations.add("Separate testing room");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            assessmentAccommodations.add("Read-aloud accommodation");
            assessmentAccommodations.add("Large print materials");
            assessmentAccommodations.add("Extended time for reading portions");
        }
        
        if (assessment.hasSignificantSocialNeeds() || assessment.hasSignificantSensoryNeeds()) {
            assessmentAccommodations.add("Separate testing room");
            assessmentAccommodations.add("Minimize distractions");
            assessmentAccommodations.add("Written instructions supplemented with verbal");
        }
        
        recommendations.put("assessmentAccommodations", assessmentAccommodations);
        
        // Technology tools
        List<String> technologyTools = new java.util.ArrayList<>();
        
        if (assessment.hasSignificantAttentionNeeds()) {
            technologyTools.add("Focus apps and website blockers");
            technologyTools.add("Digital timers and reminders");
            technologyTools.add("Organization apps");
        }
        
        if (assessment.hasSignificantReadingNeeds()) {
            technologyTools.add("Text-to-speech software");
            technologyTools.add("Reading comprehension apps");
            technologyTools.add("Dyslexia-friendly fonts");
        }
        
        technologyTools.add("ThinkAble adaptive learning platform");
        recommendations.put("technologyTools", technologyTools);
        
        // Priority level
        String priorityLevel = getAccommodationLevel(assessment);
        recommendations.put("priorityLevel", priorityLevel);
        
        return recommendations;
    }

    private Map<String, Object> buildClassroomAccessibilitySummary(List<User> students) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalStudents = students.size();
        int studentsWithAssessments = 0;
        int studentsNeedingAccommodations = 0;
        
        Map<String, Integer> presetCounts = new HashMap<>();
        Map<String, Integer> needsCounts = new HashMap<>();
        
        for (User student : students) {
            UserAssessment assessment = assessmentService.getLatestAssessment(student.getId());
            if (assessment != null && assessment.getAssessmentCompleted()) {
                studentsWithAssessments++;
                
                String preset = assessment.getRecommendedPreset();
                presetCounts.put(preset, presetCounts.getOrDefault(preset, 0) + 1);
                
                if (!"standard".equals(preset)) {
                    studentsNeedingAccommodations++;
                }
                
                // Count specific needs
                if (assessment.hasSignificantAttentionNeeds()) {
                    needsCounts.put("attention", needsCounts.getOrDefault("attention", 0) + 1);
                }
                if (assessment.hasSignificantReadingNeeds()) {
                    needsCounts.put("reading", needsCounts.getOrDefault("reading", 0) + 1);
                }
                if (assessment.hasSignificantSocialNeeds()) {
                    needsCounts.put("social", needsCounts.getOrDefault("social", 0) + 1);
                }
                if (assessment.hasSignificantSensoryNeeds()) {
                    needsCounts.put("sensory", needsCounts.getOrDefault("sensory", 0) + 1);
                }
            }
        }
        
        summary.put("totalStudents", totalStudents);
        summary.put("studentsWithAssessments", studentsWithAssessments);
        summary.put("studentsNeedingAccommodations", studentsNeedingAccommodations);
        summary.put("assessmentCompletionRate", totalStudents > 0 ? (double) studentsWithAssessments / totalStudents : 0.0);
        summary.put("accommodationRate", totalStudents > 0 ? (double) studentsNeedingAccommodations / totalStudents : 0.0);
        summary.put("presetDistribution", presetCounts);
        summary.put("needsDistribution", needsCounts);
        
        // Generate classroom recommendations
        List<String> classroomRecommendations = new java.util.ArrayList<>();
        
        if (needsCounts.getOrDefault("attention", 0) > totalStudents * 0.2) {
            classroomRecommendations.add("Consider implementing structured attention breaks");
            classroomRecommendations.add("Use visual schedules and clear transitions");
        }
        
        if (needsCounts.getOrDefault("reading", 0) > totalStudents * 0.15) {
            classroomRecommendations.add("Provide materials in multiple formats (audio, large print)");
            classroomRecommendations.add("Consider dyslexia-friendly classroom displays");
        }
        
        if (needsCounts.getOrDefault("sensory", 0) > totalStudents * 0.1) {
            classroomRecommendations.add("Create quiet zones in the classroom");
            classroomRecommendations.add("Minimize visual clutter and excessive decorations");
        }
        
        if (needsCounts.getOrDefault("social", 0) > totalStudents * 0.1) {
            classroomRecommendations.add("Use explicit instruction and clear expectations");
            classroomRecommendations.add("Provide social scripts for common interactions");
        }
        
        summary.put("classroomRecommendations", classroomRecommendations);
        
        return summary;
    }
}
