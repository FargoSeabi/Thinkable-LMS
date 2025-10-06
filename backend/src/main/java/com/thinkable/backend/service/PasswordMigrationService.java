package com.thinkable.backend.service;

import com.thinkable.backend.model.User;
import com.thinkable.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Service to migrate existing plain text passwords to BCrypt hashed passwords
 * This service automatically runs on application startup to ensure all passwords are secure
 */
@Service
public class PasswordMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Migrate all users with plain text passwords to hashed passwords
     * Note: This only works for known default passwords or when users login
     */
    @PostConstruct
    @Transactional
    public void migrateExistingPasswords() {
        try {
            logger.info("Starting password migration process...");

            // Find all users with non-hashed passwords
            List<User> usersToMigrate = userRepository.findByPasswordHashedFalseOrPasswordHashedIsNull();

            if (usersToMigrate.isEmpty()) {
                logger.info("No users need password migration.");
                return;
            }

            logger.info("Found {} users that need password migration", usersToMigrate.size());

            int migratedCount = 0;
            int skippedCount = 0;

            for (User user : usersToMigrate) {
                if (migrateKnownDefaultPassword(user)) {
                    migratedCount++;
                } else {
                    skippedCount++;
                }
            }

            logger.info("Password migration complete. Migrated: {}, Skipped: {} (will migrate on next login)",
                       migratedCount, skippedCount);

        } catch (Exception e) {
            logger.error("Error during password migration: ", e);
        }
    }

    /**
     * Migrate known default passwords immediately
     * For unknown passwords, they will be migrated when users login
     */
    private boolean migrateKnownDefaultPassword(User user) {
        // Common default passwords that might exist in the system
        String[] knownDefaults = {"password", "123456", "admin", "student", "tutor"};

        for (String defaultPassword : knownDefaults) {
            if (defaultPassword.equals(user.getPassword())) {
                // Found a known default password, hash it
                String hashedPassword = passwordEncoder.encode(defaultPassword);
                user.setPassword(hashedPassword);
                user.setPasswordHashed(true);
                userRepository.save(user);

                logger.warn("Migrated known default password for user: {} (SECURITY RISK - user should change password)",
                           user.getEmail());
                return true;
            }
        }

        // Check if password is already a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (user.getPassword() != null && user.getPassword().matches("^\\$2[ayb]\\$.{56}$")) {
            // Password is already hashed, just update the flag
            user.setPasswordHashed(true);
            userRepository.save(user);
            logger.info("Updated hash flag for user: {}", user.getEmail());
            return true;
        }

        logger.info("User {} will have password migrated on next login", user.getEmail());
        return false;
    }

    /**
     * Manual migration method for admin use
     */
    public void forcePasswordMigration(String email, String plainTextPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null && plainTextPassword.equals(user.getPassword())) {
            String hashedPassword = passwordEncoder.encode(plainTextPassword);
            user.setPassword(hashedPassword);
            user.setPasswordHashed(true);
            userRepository.save(user);
            logger.info("Manually migrated password for user: {}", email);
        }
    }

    /**
     * Get migration status
     */
    public MigrationStatus getMigrationStatus() {
        long totalUsers = userRepository.count();
        long migratedUsers = userRepository.countByPasswordHashedTrue();
        long pendingUsers = totalUsers - migratedUsers;

        return new MigrationStatus(totalUsers, migratedUsers, pendingUsers);
    }

    /**
     * Migration status data class
     */
    public static class MigrationStatus {
        private final long totalUsers;
        private final long migratedUsers;
        private final long pendingMigration;

        public MigrationStatus(long totalUsers, long migratedUsers, long pendingMigration) {
            this.totalUsers = totalUsers;
            this.migratedUsers = migratedUsers;
            this.pendingMigration = pendingMigration;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getMigratedUsers() { return migratedUsers; }
        public long getPendingMigration() { return pendingMigration; }
        public double getMigrationPercentage() {
            return totalUsers > 0 ? (double) migratedUsers / totalUsers * 100 : 100;
        }
    }
}
