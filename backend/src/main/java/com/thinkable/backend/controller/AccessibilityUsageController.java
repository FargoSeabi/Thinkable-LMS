package com.thinkable.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/accessibility")
public class AccessibilityUsageController {

    @PostMapping("/usage")
    public ResponseEntity<Map<String, Object>> trackUsage(@RequestBody Map<String, Object> usageData) {
        // Log accessibility feature usage
        System.out.println("Accessibility usage tracked: " + usageData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Usage tracked successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/text-extraction-usage")
    public ResponseEntity<Map<String, Object>> trackTextExtractionUsage(@RequestBody Map<String, Object> usageData) {
        // Log text extraction usage
        System.out.println("Text extraction usage tracked: " + usageData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Text extraction usage tracked successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tts-usage")
    public ResponseEntity<Map<String, Object>> trackTTSUsage(@RequestBody Map<String, Object> usageData) {
        // Log text-to-speech usage
        System.out.println("TTS usage tracked: " + usageData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "TTS usage tracked successfully");
        
        return ResponseEntity.ok(response);
    }
}
