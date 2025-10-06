package com.thinkable.backend.controller;

import com.thinkable.backend.entity.TutorProfile;
import com.thinkable.backend.service.TutorProfileService;
import com.thinkable.backend.service.TutorProfileService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for tutor profile management
 */
@RestController
@RequestMapping("/api/tutor/profile")
public class TutorProfileController {
    
    @Autowired
    private TutorProfileService tutorProfileService;
    
    /**
     * Create or update tutor profile
     */
    @PostMapping
    public ResponseEntity<?> createOrUpdateProfile(@RequestParam Long userId, 
                                                  @RequestBody TutorProfileRequest request) {
        try {
            TutorProfile profile = tutorProfileService.createOrUpdateProfile(userId, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Tutor profile saved successfully",
                "profileId", profile.getId(),
                "verificationStatus", profile.getVerificationStatus(),
                "displayName", profile.getDisplayName()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to save tutor profile: " + e.getMessage()));
        }
    }
    
    /**
     * Get tutor profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getTutorProfile(@PathVariable Long userId) {
        try {
            TutorProfile profile = tutorProfileService.getTutorProfile(userId);
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tutor profile not found: " + e.getMessage()));
        }
    }
    
    /**
     * Get tutor dashboard analytics
     */
    @GetMapping("/{userId}/analytics")
    public ResponseEntity<?> getTutorAnalytics(@PathVariable Long userId) {
        try {
            TutorAnalytics analytics = tutorProfileService.getTutorAnalytics(userId);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get analytics: " + e.getMessage()));
        }
    }
    
    /**
     * Search tutors
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTutors(
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "specialization", required = false) String neurodivergentSpecialization,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "hasAccessibilityExpertise", required = false) Boolean hasAccessibilityExpertise,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        
        try {
            TutorSearchRequest searchRequest = new TutorSearchRequest();
            searchRequest.setSubject(subject);
            searchRequest.setNeurodivergentSpecialization(neurodivergentSpecialization);
            searchRequest.setMinRating(minRating);
            searchRequest.setMinExperience(minExperience);
            searchRequest.setHasAccessibilityExpertise(hasAccessibilityExpertise);
            searchRequest.setLimit(limit);
            
            List<TutorProfile> tutors = tutorProfileService.searchTutors(searchRequest);
            
            return ResponseEntity.ok(Map.of(
                "tutors", tutors,
                "count", tutors.size(),
                "searchCriteria", Map.of(
                    "subject", subject,
                    "specialization", neurodivergentSpecialization,
                    "minRating", minRating,
                    "hasAccessibilityExpertise", hasAccessibilityExpertise
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to search tutors: " + e.getMessage()));
        }
    }
    
    /**
     * Get all verified tutors
     */
    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedTutors() {
        try {
            List<TutorProfile> tutors = tutorProfileService.getVerifiedTutors();
            
            return ResponseEntity.ok(Map.of(
                "tutors", tutors,
                "count", tutors.size(),
                "message", "All verified tutors"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get verified tutors: " + e.getMessage()));
        }
    }
    
    /**
     * Get top-rated tutors
     */
    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedTutors(@RequestParam(value = "minRating", defaultValue = "4.0") Double minRating) {
        try {
            List<TutorProfile> tutors = tutorProfileService.getTopRatedTutors(minRating);
            
            return ResponseEntity.ok(Map.of(
                "tutors", tutors,
                "count", tutors.size(),
                "minRating", minRating,
                "message", "Top-rated tutors"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get top-rated tutors: " + e.getMessage()));
        }
    }
    
    /**
     * Get tutors by subject
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<?> getTutorsBySubject(@PathVariable String subject) {
        try {
            List<TutorProfile> tutors = tutorProfileService.findTutorsBySubject(subject);
            
            return ResponseEntity.ok(Map.of(
                "tutors", tutors,
                "count", tutors.size(),
                "subject", subject,
                "message", String.format("Tutors specializing in %s", subject)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to find tutors by subject: " + e.getMessage()));
        }
    }
    
    /**
     * Get neurodivergent specialists
     */
    @GetMapping("/specialists/{specialization}")
    public ResponseEntity<?> getNeurodivergentSpecialists(@PathVariable String specialization) {
        try {
            List<TutorProfile> tutors = tutorProfileService.findNeurodivergentSpecialists(specialization);
            
            return ResponseEntity.ok(Map.of(
                "tutors", tutors,
                "count", tutors.size(),
                "specialization", specialization,
                "message", String.format("Tutors specializing in %s support", specialization)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to find neurodivergent specialists: " + e.getMessage()));
        }
    }
    
    /**
     * Update tutor verification status (admin only)
     */
    @PostMapping("/{tutorId}/verify")
    public ResponseEntity<?> verifyTutor(@PathVariable Long tutorId, 
                                        @RequestParam String status,
                                        @RequestParam(required = false) String notes) {
        try {
            TutorProfile tutor = tutorProfileService.verifyTutor(tutorId, status, notes);
            
            return ResponseEntity.ok(Map.of(
                "message", "Tutor verification status updated",
                "tutorId", tutorId,
                "status", tutor.getVerificationStatus(),
                "displayName", tutor.getDisplayName()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to update verification status: " + e.getMessage()));
        }
    }
    
    /**
     * Update tutor rating
     */
    @PostMapping("/{tutorId}/rating")
    public ResponseEntity<?> updateTutorRating(@PathVariable Long tutorId,
                                              @RequestParam Double rating,
                                              @RequestParam Integer totalRatings) {
        try {
            tutorProfileService.updateTutorRating(tutorId, rating, totalRatings);
            
            return ResponseEntity.ok(Map.of(
                "message", "Tutor rating updated successfully",
                "tutorId", tutorId,
                "newRating", rating
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to update rating: " + e.getMessage()));
        }
    }
    
    /**
     * Get platform statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getPlatformStatistics() {
        try {
            List<TutorProfile> verifiedTutors = tutorProfileService.getVerifiedTutors();
            
            Map<String, Object> stats = Map.of(
                "totalVerifiedTutors", verifiedTutors.size(),
                "averageRating", verifiedTutors.stream()
                        .mapToDouble(TutorProfile::getRatingAverage)
                        .average()
                        .orElse(0.0),
                "accessibilitySpecialists", verifiedTutors.stream()
                        .mapToLong(t -> t.hasAccessibilityExpertise() ? 1 : 0)
                        .sum(),
                "experiencedTutors", verifiedTutors.stream()
                        .mapToLong(t -> t.isExperienced() ? 1 : 0)
                        .sum()
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to get statistics: " + e.getMessage()));
        }
    }
}
