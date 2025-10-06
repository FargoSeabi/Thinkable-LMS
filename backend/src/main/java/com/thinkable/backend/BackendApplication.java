package com.thinkable.backend;

import com.thinkable.backend.model.User;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.entity.TutorProfile;
import com.thinkable.backend.repository.TutorProfileRepository;
import com.thinkable.backend.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TutorProfileRepository tutorProfileRepository;
    
    @Autowired
    private AchievementService achievementService;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get("uploads"));
            logger.info("Uploads directory created successfully.");
        } catch (IOException e) {
            logger.error("Failed to create uploads directory", e);
        }

        if (userRepository.findByEmail("admin@example.com") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            logger.info("Admin user initialized.");
        }

        if (userRepository.findByEmail("student@example.com") == null) {
            User student = new User();
            student.setUsername("student");
            student.setEmail("student@example.com");
            student.setPassword("stu123");
            student.setRole("STUDENT");
            userRepository.save(student);
            logger.info("Student user initialized.");
        }

        User tutorUser = userRepository.findByEmail("tutor@example.com");
        if (tutorUser == null) {
            User tutor = new User();
            tutor.setUsername("tutoruser");
            tutor.setEmail("tutor@example.com");
            tutor.setPassword("tutor123");
            tutor.setRole("TUTOR");
            tutorUser = userRepository.save(tutor);
            logger.info("Tutor user initialized.");
        }
        
        // Always check if tutor profile exists and create if needed
        if (tutorProfileRepository.findByUserId(tutorUser.getId()).isEmpty()) {
            TutorProfile tutorProfile = new TutorProfile();
            tutorProfile.setUserId(tutorUser.getId());
            tutorProfile.setDisplayName("Demo Tutor");
            tutorProfile.setBio("A demonstration tutor account for testing the ThinkAble platform.");
            tutorProfile.setQualifications("Bachelor's in Education, Special Education Certification");
            tutorProfile.setTeachingExperienceYears(5);
            tutorProfile.setNeurodivergentSpecialization("[\"ADHD\", \"Autism\", \"Dyslexia\"]");
            tutorProfile.setSubjectExpertise("[\"Math\", \"Science\", \"English\"]");
            tutorProfile.setTeachingStyles("[\"Visual Learning\", \"Interactive\", \"Structured\"]");
            tutorProfile.setAccessibilityExpertise("[\"Screen Readers\", \"Large Text\", \"Color Contrast\"]");
            tutorProfile.setVerificationStatus("verified");
            tutorProfile.setIsActive(true);
            tutorProfileRepository.save(tutorProfile);
            logger.info("Tutor profile initialized for user ID: " + tutorUser.getId());
        }
        
        // Initialize default achievements
        achievementService.initializeDefaultAchievements();
    }
}
