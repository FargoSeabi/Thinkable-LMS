package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tutor Profile Entity
 * Stores tutor information, credentials, and teaching specializations
 */
@Entity
@Table(name = "tutor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // Links to main User entity
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications;
    
    @Column(name = "teaching_experience_years")
    private Integer teachingExperienceYears;
    
    @Column(name = "neurodivergent_specialization", length = 500)
    private String neurodivergentSpecialization; // JSON array of specializations
    
    @Column(name = "subject_expertise", length = 500)
    private String subjectExpertise; // JSON array of subjects
    
    @Column(name = "teaching_styles", length = 300)
    private String teachingStyles; // JSON array of teaching approaches
    
    @Column(name = "accessibility_expertise", length = 400)
    private String accessibilityExpertise; // JSON array of accessibility knowledge
    
    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "pending"; // pending, verified, rejected
    
    @Column(name = "rating_average")
    private Double ratingAverage = 0.0;
    
    @Column(name = "total_students")
    private Integer totalStudents = 0;
    
    @Column(name = "content_count")
    private Integer contentCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "hourly_rate")
    private Double hourlyRate;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "availability_schedule", columnDefinition = "TEXT")
    private String availabilitySchedule; // JSON schedule
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Relationships
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("tutor-contents")
    private List<LearningContent> contents;
    
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("tutor-reviews")
    private List<TutorReview> reviews;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isVerified() {
        return "verified".equals(verificationStatus);
    }
    
    public boolean isExperienced() {
        return teachingExperienceYears != null && teachingExperienceYears >= 3;
    }
    
    public boolean isHighlyRated() {
        return ratingAverage != null && ratingAverage >= 4.5;
    }
    
    public boolean hasAccessibilityExpertise() {
        return accessibilityExpertise != null && !accessibilityExpertise.trim().isEmpty();
    }
    
    public boolean hasNeurodivergentSpecialization() {
        return neurodivergentSpecialization != null && !neurodivergentSpecialization.trim().isEmpty();
    }
}
