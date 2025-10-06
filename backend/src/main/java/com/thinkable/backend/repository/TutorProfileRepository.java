package com.thinkable.backend.repository;

import com.thinkable.backend.entity.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {
    
    Optional<TutorProfile> findByUserId(Long userId);
    
    List<TutorProfile> findByIsActiveTrue();
    
    List<TutorProfile> findByVerificationStatus(String status);
    
    List<TutorProfile> findByIsActiveTrueAndVerificationStatus(String status);
    
    @Query("SELECT t FROM TutorProfile t WHERE t.isActive = true AND t.verificationStatus = 'verified' AND t.ratingAverage >= :minRating ORDER BY t.ratingAverage DESC")
    List<TutorProfile> findTopRatedTutors(@Param("minRating") Double minRating);
    
    @Query("SELECT t FROM TutorProfile t WHERE t.isActive = true AND t.verificationStatus = 'verified' AND " +
           "LOWER(t.subjectExpertise) LIKE LOWER(CONCAT('%', :subject, '%'))")
    List<TutorProfile> findTutorsBySubject(@Param("subject") String subject);
    
    @Query("SELECT t FROM TutorProfile t WHERE t.isActive = true AND t.verificationStatus = 'verified' AND " +
           "LOWER(t.neurodivergentSpecialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    List<TutorProfile> findTutorsByNeurodivergentSpecialization(@Param("specialization") String specialization);
    
    @Query("SELECT t FROM TutorProfile t WHERE t.isActive = true AND " +
           "t.accessibilityExpertise IS NOT NULL AND t.accessibilityExpertise != ''")
    List<TutorProfile> findTutorsWithAccessibilityExpertise();
    
    @Query("SELECT COUNT(t) FROM TutorProfile t WHERE t.verificationStatus = 'verified'")
    Long countVerifiedTutors();
    
    @Query("SELECT AVG(t.ratingAverage) FROM TutorProfile t WHERE t.ratingAverage > 0")
    Double getAverageRating();
}
