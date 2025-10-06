package com.thinkable.backend.controller;

import com.thinkable.backend.service.UserPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/user-preferences")
public class UserPreferencesController {

    private static final Logger logger = LoggerFactory.getLogger(UserPreferencesController.class);

    @Autowired
    private UserPreferencesService userPreferencesService;

    @GetMapping
    public ResponseEntity<?> getUserPreferences(@RequestParam String username) {
        try {
            logger.info("Getting user preferences for: {}", username);
            Map<String, Object> preferences = userPreferencesService.getUserPreferences(username);
            logger.info("Successfully retrieved preferences for: {}", username);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            logger.error("Error retrieving user preferences for {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving user preferences: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> saveUserPreferences(
            @RequestParam String username,
            @RequestBody Map<String, Object> preferences) {
        try {
            userPreferencesService.saveUserPreferences(username, preferences);
            return ResponseEntity.ok().body(Map.of("message", "Preferences saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving user preferences: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPresetHistory(@RequestParam String username) {
        try {
            Map<String, Object> history = userPreferencesService.getPresetHistory(username);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving preset history: " + e.getMessage());
        }
    }

    @PostMapping("/log-preset-usage")
    public ResponseEntity<?> logPresetUsage(
            @RequestParam String username,
            @RequestBody Map<String, String> usageData) {
        try {
            String presetName = usageData.get("presetName");
            userPreferencesService.logPresetUsage(username, presetName);
            return ResponseEntity.ok().body(Map.of("message", "Usage logged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error logging preset usage: " + e.getMessage());
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<?> resetUserPreferences(@RequestParam String username) {
        try {
            userPreferencesService.resetUserPreferences(username);
            return ResponseEntity.ok().body(Map.of("message", "Preferences reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resetting preferences: " + e.getMessage());
        }
    }
}
