package com.thinkable.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.thinkable.backend.model.User;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.entity.TutorProfile;
import com.thinkable.backend.repository.TutorProfileRepository;
import com.thinkable.backend.controller.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            LOGGER.info("Received registration: {}", user);
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Password is required"));
            }
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
            }

            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                String baseUsername = user.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                user.setUsername(baseUsername);
                int counter = 1;
                while (userRepository.findByUsername(user.getUsername()) != null) {
                    user.setUsername(baseUsername + counter);
                    counter++;
                }
            }

            user.setCreatedAt(java.time.LocalDateTime.now());
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("STUDENT");
            }

            // Hash the password before saving
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
            user.setPasswordHashed(true);

            userRepository.save(user);
            return ResponseEntity.ok(new SuccessResponse("User registered successfully"));

        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error("Registration error: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Registration error: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LOGGER.info("Received login request: {}", loginRequest);
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Password is required"));
            }
            User user = userRepository.findByEmail(loginRequest.getEmail().trim());

            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email or password"));
            }

            // Check password based on whether it's hashed or not
            boolean passwordValid = false;
            if (user.getPasswordHashed() != null && user.getPasswordHashed()) {
                // Password is hashed, use BCrypt to verify
                passwordValid = passwordEncoder.matches(loginRequest.getPassword().trim(), user.getPassword());
            } else {
                // Legacy plain text password - verify and migrate to hashed
                if (loginRequest.getPassword().trim().equals(user.getPassword())) {
                    passwordValid = true;
                    // Migrate to hashed password
                    String hashedPassword = passwordEncoder.encode(loginRequest.getPassword().trim());
                    user.setPassword(hashedPassword);
                    user.setPasswordHashed(true);
                    userRepository.save(user);
                    LOGGER.info("Migrated plain text password to hashed for user: {}", user.getEmail());
                }
            }

            if (!passwordValid) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email or password"));
            }

            // Create a map with more than 3 entries
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", jwtUtil.generateToken(user.getEmail()));
            responseData.put("role", user.getRole());
            responseData.put("email", user.getEmail());
            responseData.put("expiresIn", jwtUtil.getExpirationTime());

            LOGGER.info("Login successful for user: {}. Token expiry: {}",
                    user.getEmail(), jwtUtil.getExpirationTime());

            return ResponseEntity.ok(responseData);

        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error("Login error: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Login error: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Login failed"));
        }
    }
    
    @PostMapping("/register/tutor")
    public ResponseEntity<?> registerTutor(@RequestBody TutorRegistrationRequest request) {
        try {
            LOGGER.info("Received tutor registration: {}", request.getEmail());
            
            // Validate required fields
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Password is required"));
            }
            if (request.getDisplayName() == null || request.getDisplayName().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Display name is required"));
            }
            
            // Check if user already exists
            if (userRepository.findByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
            }

            // Create User entity
            User user = new User();
            user.setEmail(request.getEmail());

            // Hash the password before saving
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(hashedPassword);
            user.setPasswordHashed(true);

            user.setRole("TUTOR");
            user.setCreatedAt(java.time.LocalDateTime.now());
            
            // Generate unique username
            String baseUsername = request.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            user.setUsername(baseUsername);
            int counter = 1;
            while (userRepository.findByUsername(user.getUsername()) != null) {
                user.setUsername(baseUsername + counter);
                counter++;
            }
            
            // Save user first
            user = userRepository.save(user);
            
            // Create TutorProfile
            TutorProfile tutorProfile = new TutorProfile();
            tutorProfile.setUserId(user.getId());
            tutorProfile.setDisplayName(request.getDisplayName());
            tutorProfile.setBio(request.getBio());
            tutorProfile.setQualifications(request.getQualifications());
            tutorProfile.setTeachingExperienceYears(request.getTeachingExperienceYears());
            tutorProfile.setSubjectExpertise(request.getSubjectExpertise());
            tutorProfile.setNeurodivergentSpecialization(request.getNeurodivergentSpecialization());
            tutorProfile.setTeachingStyles(request.getTeachingStyles());
            tutorProfile.setAccessibilityExpertise(request.getAccessibilityExpertise());
            tutorProfile.setVerificationStatus("pending");
            tutorProfile.setIsActive(true);
            tutorProfile.setContentCount(0);
            
            tutorProfileRepository.save(tutorProfile);
            
            LOGGER.info("Tutor registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(new SuccessResponse("Tutor account created successfully"));

        } catch (Exception e) {
            LOGGER.error("Tutor registration error: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("username", user.getUsername());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            LOGGER.error("Error getting user profile: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Failed to get profile"));
        }
    }

    public static class LoginResponse {
        public String role;
        public String email;

        public LoginResponse(String role, String email) {
            this.role = role;
            this.email = email;
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }
    
    public static class LoginRequest {
        private String email;
        private String password;
        
        public LoginRequest() {}
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class TutorRegistrationRequest {
        private String displayName;
        private String email;
        private String password;
        private String bio;
        private String qualifications;
        private Integer teachingExperienceYears;
        private String subjectExpertise;
        private String neurodivergentSpecialization;
        private String teachingStyles;
        private String accessibilityExpertise;
        
        // Getters and setters
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getQualifications() { return qualifications; }
        public void setQualifications(String qualifications) { this.qualifications = qualifications; }
        public Integer getTeachingExperienceYears() { return teachingExperienceYears; }
        public void setTeachingExperienceYears(Integer teachingExperienceYears) { this.teachingExperienceYears = teachingExperienceYears; }
        public String getSubjectExpertise() { return subjectExpertise; }
        public void setSubjectExpertise(String subjectExpertise) { this.subjectExpertise = subjectExpertise; }
        public String getNeurodivergentSpecialization() { return neurodivergentSpecialization; }
        public void setNeurodivergentSpecialization(String neurodivergentSpecialization) { this.neurodivergentSpecialization = neurodivergentSpecialization; }
        public String getTeachingStyles() { return teachingStyles; }
        public void setTeachingStyles(String teachingStyles) { this.teachingStyles = teachingStyles; }
        public String getAccessibilityExpertise() { return accessibilityExpertise; }
        public void setAccessibilityExpertise(String accessibilityExpertise) { this.accessibilityExpertise = accessibilityExpertise; }
    }
}
