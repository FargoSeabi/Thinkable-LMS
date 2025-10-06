package com.thinkable.backend.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * User Tool Usage Entity
 * Tracks how individual users interact with neurodivergent support tools
 */
@Entity
@Table(name = "user_tool_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserToolUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "tool_name", nullable = false, length = 50)
    private String toolName;
    
    @Column(name = "tool_context", length = 100)
    private String toolContext;
    
    @Column(name = "usage_timestamp")
    private LocalDateTime usageTimestamp = LocalDateTime.now();
    
    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;
    
    @Column(name = "success_rating") // 1-10 scale
    private Integer successRating;
    
    @Column(name = "user_energy_level") // 1-10 scale
    private Integer userEnergyLevel;
    
    @Column(name = "time_of_day", length = 20)
    private String timeOfDay;
    
    @Column(name = "day_of_week") // 0-6 (Sunday=0)
    private Integer dayOfWeek;
    
    @Column(name = "activity_context", length = 50)
    private String activityContext;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
    
    // Helper methods
    public String getTimeSlot() {
        if (usageTimestamp == null) return "unknown";
        
        int hour = usageTimestamp.getHour();
        if (hour < 6) return "late_night";
        if (hour < 9) return "early_morning";
        if (hour < 12) return "late_morning";
        if (hour < 15) return "early_afternoon";
        if (hour < 18) return "late_afternoon";
        if (hour < 21) return "early_evening";
        return "late_evening";
    }
    
    public boolean isSuccessful() {
        return successRating != null && successRating >= 7;
    }
    
    public boolean isHighEnergyUsage() {
        return userEnergyLevel != null && userEnergyLevel >= 8;
    }
    
    public boolean isLowEnergyUsage() {
        return userEnergyLevel != null && userEnergyLevel <= 3;
    }
}
