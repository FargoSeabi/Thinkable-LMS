package com.thinkable.backend.service;

import java.util.stream.Collectors;

import com.thinkable.backend.entity.TutorProfile;
import com.thinkable.backend.repository.TutorProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing tutor profiles and verification
 */
@Service
@Transactional
public class TutorProfileService {
    
    @Autowired
    private TutorProfileRepository tutorRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create or update tutor profile
     */
    public TutorProfile createOrUpdateProfile(Long userId, TutorProfileRequest request) {
        Optional<TutorProfile> existingProfile = tutorRepository.findByUserId(userId);
        
        TutorProfile profile = existingProfile.orElse(new TutorProfile());
        profile.setUserId(userId);
        updateProfileFromRequest(profile, request);
        
        return tutorRepository.save(profile);
    }
    
    /**
     * Get tutor profile by user ID
     */
    public TutorProfile getTutorProfile(Long userId) {
        return tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor profile not found"));
    }
    
    /**
     * Get all verified tutors
     */
    public List<TutorProfile> getVerifiedTutors() {
        return tutorRepository.findByIsActiveTrueAndVerificationStatus("verified");
    }
    
    /**
     * Search tutors by subject expertise
     */
    public List<TutorProfile> findTutorsBySubject(String subject) {
        return tutorRepository.findTutorsBySubject(subject);
    }
    
    /**
     * Find tutors with neurodivergent specialization
     */
    public List<TutorProfile> findNeurodivergentSpecialists(String specialization) {
        return tutorRepository.findTutorsByNeurodivergentSpecialization(specialization);
    }
    
    /**
     * Get top-rated tutors
     */
    public List<TutorProfile> getTopRatedTutors(double minRating) {
        return tutorRepository.findTopRatedTutors(minRating);
    }
    
    /**
     * Verify tutor credentials
     */
    public TutorProfile verifyTutor(Long tutorId, String verificationStatus, String notes) {
        TutorProfile tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor not found"));
        
        tutor.setVerificationStatus(verificationStatus);
        tutor.setUpdatedAt(LocalDateTime.now());
        
        return tutorRepository.save(tutor);
    }
    
    /**
     * Update tutor rating
     */
    public void updateTutorRating(Long tutorId, double newRating, int totalRatings) {
        TutorProfile tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor not found"));
        
        // Calculate new average rating
        double currentTotal = tutor.getRatingAverage() * (totalRatings - 1);
        double newAverage = (currentTotal + newRating) / totalRatings;
        
        tutor.setRatingAverage(newAverage);
        tutorRepository.save(tutor);
    }
    
    /**
     * Get tutor dashboard analytics
     */
    public TutorAnalytics getTutorAnalytics(Long userId) {
        TutorProfile tutor = getTutorProfile(userId);
        
        TutorAnalytics analytics = new TutorAnalytics();
        analytics.setTutorId(tutor.getId());
        analytics.setDisplayName(tutor.getDisplayName());
        analytics.setVerificationStatus(tutor.getVerificationStatus());
        analytics.setRatingAverage(tutor.getRatingAverage());
        analytics.setTotalStudents(tutor.getTotalStudents());
        analytics.setContentCount(tutor.getContentCount());
        analytics.setIsVerified(tutor.isVerified());
        analytics.setIsHighlyRated(tutor.isHighlyRated());
        analytics.setHasAccessibilityExpertise(tutor.hasAccessibilityExpertise());
        
        // Parse JSON fields
        try {
            if (tutor.getSubjectExpertise() != null) {
                List<String> subjects = objectMapper.readValue(tutor.getSubjectExpertise(), List.class);
                analytics.setSubjectExpertise(subjects);
            }
            
            if (tutor.getNeurodivergentSpecialization() != null) {
                List<String> specializations = objectMapper.readValue(tutor.getNeurodivergentSpecialization(), List.class);
                analytics.setNeurodivergentSpecializations(specializations);
            }
            
            if (tutor.getAccessibilityExpertise() != null) {
                List<String> expertise = objectMapper.readValue(tutor.getAccessibilityExpertise(), List.class);
                analytics.setAccessibilityExpertise(expertise);
            }
        } catch (JsonProcessingException e) {
            // Log error but continue
        }
        
        return analytics;
    }
    
    /**
     * Search and filter tutors
     */
    public List<TutorProfile> searchTutors(TutorSearchRequest searchRequest) {
        List<TutorProfile> tutors = getVerifiedTutors();
        
        return tutors.stream()
                .filter(tutor -> matchesSearchCriteria(tutor, searchRequest))
                .sorted((t1, t2) -> Double.compare(t2.getRatingAverage(), t1.getRatingAverage()))
                .limit(searchRequest.getLimit() != null ? searchRequest.getLimit() : 20)
                .collect(Collectors.toList());
    }
    
    // Private helper methods
    
    private void updateProfileFromRequest(TutorProfile profile, TutorProfileRequest request) {
        profile.setDisplayName(request.getDisplayName());
        profile.setBio(request.getBio());
        profile.setQualifications(request.getQualifications());
        profile.setTeachingExperienceYears(request.getTeachingExperienceYears());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setTimezone(request.getTimezone());
        
        // Convert lists to JSON strings
        try {
            if (request.getNeurodivergentSpecializations() != null) {
                profile.setNeurodivergentSpecialization(
                        objectMapper.writeValueAsString(request.getNeurodivergentSpecializations()));
            }
            
            if (request.getSubjectExpertise() != null) {
                profile.setSubjectExpertise(
                        objectMapper.writeValueAsString(request.getSubjectExpertise()));
            }
            
            if (request.getTeachingStyles() != null) {
                profile.setTeachingStyles(
                        objectMapper.writeValueAsString(request.getTeachingStyles()));
            }
            
            if (request.getAccessibilityExpertise() != null) {
                profile.setAccessibilityExpertise(
                        objectMapper.writeValueAsString(request.getAccessibilityExpertise()));
            }
            
            if (request.getAvailabilitySchedule() != null) {
                profile.setAvailabilitySchedule(
                        objectMapper.writeValueAsString(request.getAvailabilitySchedule()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing profile data", e);
        }
        
        profile.setUpdatedAt(LocalDateTime.now());
    }
    
    private boolean matchesSearchCriteria(TutorProfile tutor, TutorSearchRequest request) {
        // Subject expertise filter
        if (request.getSubject() != null) {
            try {
                List<String> subjects = objectMapper.readValue(tutor.getSubjectExpertise(), List.class);
                if (subjects.stream().noneMatch(s -> s.toLowerCase().contains(request.getSubject().toLowerCase()))) {
                    return false;
                }
            } catch (JsonProcessingException e) {
                return false;
            }
        }
        
        // Neurodivergent specialization filter
        if (request.getNeurodivergentSpecialization() != null) {
            try {
                List<String> specializations = objectMapper.readValue(tutor.getNeurodivergentSpecialization(), List.class);
                if (specializations.stream().noneMatch(s -> s.toLowerCase()
                        .contains(request.getNeurodivergentSpecialization().toLowerCase()))) {
                    return false;
                }
            } catch (JsonProcessingException e) {
                return false;
            }
        }
        
        // Rating filter
        if (request.getMinRating() != null && tutor.getRatingAverage() < request.getMinRating()) {
            return false;
        }
        
        // Experience filter
        if (request.getMinExperience() != null && 
            (tutor.getTeachingExperienceYears() == null || tutor.getTeachingExperienceYears() < request.getMinExperience())) {
            return false;
        }
        
        // Accessibility expertise filter
        if (Boolean.TRUE.equals(request.getHasAccessibilityExpertise()) && !tutor.hasAccessibilityExpertise()) {
            return false;
        }
        
        return true;
    }
    
    // Inner classes for DTOs
    
    public static class TutorProfileRequest {
        private String displayName;
        private String bio;
        private String qualifications;
        private Integer teachingExperienceYears;
        private List<String> neurodivergentSpecializations;
        private List<String> subjectExpertise;
        private List<String> teachingStyles;
        private List<String> accessibilityExpertise;
        private Double hourlyRate;
        private String timezone;
        private Map<String, Object> availabilitySchedule;
        
        // Getters and setters
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getQualifications() { return qualifications; }
        public void setQualifications(String qualifications) { this.qualifications = qualifications; }
        public Integer getTeachingExperienceYears() { return teachingExperienceYears; }
        public void setTeachingExperienceYears(Integer teachingExperienceYears) { this.teachingExperienceYears = teachingExperienceYears; }
        public List<String> getNeurodivergentSpecializations() { return neurodivergentSpecializations; }
        public void setNeurodivergentSpecializations(List<String> neurodivergentSpecializations) { this.neurodivergentSpecializations = neurodivergentSpecializations; }
        public List<String> getSubjectExpertise() { return subjectExpertise; }
        public void setSubjectExpertise(List<String> subjectExpertise) { this.subjectExpertise = subjectExpertise; }
        public List<String> getTeachingStyles() { return teachingStyles; }
        public void setTeachingStyles(List<String> teachingStyles) { this.teachingStyles = teachingStyles; }
        public List<String> getAccessibilityExpertise() { return accessibilityExpertise; }
        public void setAccessibilityExpertise(List<String> accessibilityExpertise) { this.accessibilityExpertise = accessibilityExpertise; }
        public Double getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public Map<String, Object> getAvailabilitySchedule() { return availabilitySchedule; }
        public void setAvailabilitySchedule(Map<String, Object> availabilitySchedule) { this.availabilitySchedule = availabilitySchedule; }
    }
    
    public static class TutorSearchRequest {
        private String subject;
        private String neurodivergentSpecialization;
        private Double minRating;
        private Integer minExperience;
        private Boolean hasAccessibilityExpertise;
        private Integer limit;
        
        // Getters and setters
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getNeurodivergentSpecialization() { return neurodivergentSpecialization; }
        public void setNeurodivergentSpecialization(String neurodivergentSpecialization) { this.neurodivergentSpecialization = neurodivergentSpecialization; }
        public Double getMinRating() { return minRating; }
        public void setMinRating(Double minRating) { this.minRating = minRating; }
        public Integer getMinExperience() { return minExperience; }
        public void setMinExperience(Integer minExperience) { this.minExperience = minExperience; }
        public Boolean getHasAccessibilityExpertise() { return hasAccessibilityExpertise; }
        public void setHasAccessibilityExpertise(Boolean hasAccessibilityExpertise) { this.hasAccessibilityExpertise = hasAccessibilityExpertise; }
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }
    
    public static class TutorAnalytics {
        private Long tutorId;
        private String displayName;
        private String verificationStatus;
        private Double ratingAverage;
        private Integer totalStudents;
        private Integer contentCount;
        private Boolean isVerified;
        private Boolean isHighlyRated;
        private Boolean hasAccessibilityExpertise;
        private List<String> subjectExpertise;
        private List<String> neurodivergentSpecializations;
        private List<String> accessibilityExpertise;
        
        // Getters and setters
        public Long getTutorId() { return tutorId; }
        public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getVerificationStatus() { return verificationStatus; }
        public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
        public Double getRatingAverage() { return ratingAverage; }
        public void setRatingAverage(Double ratingAverage) { this.ratingAverage = ratingAverage; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public Integer getContentCount() { return contentCount; }
        public void setContentCount(Integer contentCount) { this.contentCount = contentCount; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public Boolean getIsHighlyRated() { return isHighlyRated; }
        public void setIsHighlyRated(Boolean isHighlyRated) { this.isHighlyRated = isHighlyRated; }
        public Boolean getHasAccessibilityExpertise() { return hasAccessibilityExpertise; }
        public void setHasAccessibilityExpertise(Boolean hasAccessibilityExpertise) { this.hasAccessibilityExpertise = hasAccessibilityExpertise; }
        public List<String> getSubjectExpertise() { return subjectExpertise; }
        public void setSubjectExpertise(List<String> subjectExpertise) { this.subjectExpertise = subjectExpertise; }
        public List<String> getNeurodivergentSpecializations() { return neurodivergentSpecializations; }
        public void setNeurodivergentSpecializations(List<String> neurodivergentSpecializations) { this.neurodivergentSpecializations = neurodivergentSpecializations; }
        public List<String> getAccessibilityExpertise() { return accessibilityExpertise; }
        public void setAccessibilityExpertise(List<String> accessibilityExpertise) { this.accessibilityExpertise = accessibilityExpertise; }
    }
}
