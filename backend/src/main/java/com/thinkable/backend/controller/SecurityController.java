package com.thinkable.backend.controller;

import com.thinkable.backend.service.PasswordMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for security-related administrative functions
 */
@RestController
@RequestMapping("/api/admin/security")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @Autowired
    private PasswordMigrationService passwordMigrationService;

    /**
     * Get password migration status
     */
    @GetMapping("/password-migration/status")
    public ResponseEntity<Map<String, Object>> getPasswordMigrationStatus() {
        try {
            PasswordMigrationService.MigrationStatus status = passwordMigrationService.getMigrationStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", status.getTotalUsers());
            response.put("migratedUsers", status.getMigratedUsers());
            response.put("pendingMigration", status.getPendingMigration());
            response.put("migrationPercentage", Math.round(status.getMigrationPercentage() * 100) / 100.0);

            String statusMessage;
            if (status.getPendingMigration() == 0) {
                statusMessage = "All passwords are securely hashed";
            } else {
                statusMessage = String.format("%d users will have passwords migrated on next login",
                                            status.getPendingMigration());
            }

            response.put("status", statusMessage);
            response.put("securityLevel", status.getPendingMigration() == 0 ? "SECURE" : "MIGRATION_PENDING");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting migration status: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get migration status");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Trigger manual password migration (use with caution)
     */
    @PostMapping("/password-migration/trigger")
    public ResponseEntity<Map<String, Object>> triggerPasswordMigration() {
        try {
            passwordMigrationService.migrateExistingPasswords();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password migration triggered successfully");
            response.put("status", "completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error triggering migration: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to trigger migration");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Security health check
     */
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> securityHealthCheck() {
        try {
            PasswordMigrationService.MigrationStatus status = passwordMigrationService.getMigrationStatus();

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> passwordSecurity = new HashMap<>();

            passwordSecurity.put("totalUsers", status.getTotalUsers());
            passwordSecurity.put("hashedPasswords", status.getMigratedUsers());
            passwordSecurity.put("plaintextPasswords", status.getPendingMigration());
            passwordSecurity.put("securityCompliance", status.getPendingMigration() == 0 ? "COMPLIANT" : "NON_COMPLIANT");

            response.put("passwordSecurity", passwordSecurity);
            response.put("overallSecurityLevel", status.getPendingMigration() == 0 ? "SECURE" : "REQUIRES_ATTENTION");
            response.put("recommendations", status.getPendingMigration() > 0 ?
                         "Some users have plain text passwords. They will be migrated on next login." :
                         "All passwords are securely hashed with BCrypt.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during security health check: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Security health check failed");
            errorResponse.put("overallSecurityLevel", "UNKNOWN");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
