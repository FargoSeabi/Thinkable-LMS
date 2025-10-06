package com.thinkable.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "adaptive_ui_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AdaptiveUISettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "ui_preset", length = 50)
    private String uiPreset = "standard";

    @Column(name = "font_family", length = 100)
    private String fontFamily = "Arial, sans-serif";

    @Column(name = "font_size")
    private Integer fontSize = 16;

    @Column(name = "line_height", precision = 3, scale = 1)
    private BigDecimal lineHeight = new BigDecimal("1.5");

    @Column(name = "background_color", length = 20)
    private String backgroundColor = "#ffffff";

    @Column(name = "text_color", length = 20)
    private String textColor = "#333333";

    @Column(name = "contrast_level", length = 20)
    private String contrastLevel = "normal";

    @Column(name = "animations_enabled")
    private Boolean animationsEnabled = true;

    @Column(name = "timer_style", length = 20)
    private String timerStyle = "standard";

    @Column(name = "break_intervals")
    private Integer breakIntervals = 25; // minutes

    @Column(name = "auto_applied")
    private Boolean autoApplied = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Constructors
    public AdaptiveUISettings() {
        this.lastUpdated = LocalDateTime.now();
    }

    public AdaptiveUISettings(Long userId) {
        this();
        this.userId = userId;
    }

    public AdaptiveUISettings(Long userId, String uiPreset) {
        this(userId);
        this.uiPreset = uiPreset;
        applyPresetDefaults(uiPreset);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUiPreset() {
        return uiPreset;
    }

    public void setUiPreset(String uiPreset) {
        this.uiPreset = uiPreset;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public BigDecimal getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(BigDecimal lineHeight) {
        this.lineHeight = lineHeight;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getContrastLevel() {
        return contrastLevel;
    }

    public void setContrastLevel(String contrastLevel) {
        this.contrastLevel = contrastLevel;
    }

    public Boolean getAnimationsEnabled() {
        return animationsEnabled;
    }

    public void setAnimationsEnabled(Boolean animationsEnabled) {
        this.animationsEnabled = animationsEnabled;
    }

    public String getTimerStyle() {
        return timerStyle;
    }

    public void setTimerStyle(String timerStyle) {
        this.timerStyle = timerStyle;
    }

    public Integer getBreakIntervals() {
        return breakIntervals;
    }

    public void setBreakIntervals(Integer breakIntervals) {
        this.breakIntervals = breakIntervals;
    }

    public Boolean getAutoApplied() {
        return autoApplied;
    }

    public void setAutoApplied(Boolean autoApplied) {
        this.autoApplied = autoApplied;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public void applyPresetDefaults(String preset) {
        switch (preset.toLowerCase()) {
            case "adhd":
                applyADHDDefaults();
                break;
            case "dyslexia":
                applyDyslexiaDefaults();
                break;
            case "autism":
                applyAutismDefaults();
                break;
            case "sensory":
                applySensoryDefaults();
                break;
            case "dyslexia-adhd":
                applyDyslexiaDefaults();
                applyADHDTimerDefaults();
                break;
            default:
                applyStandardDefaults();
                break;
        }
    }

    private void applyADHDDefaults() {
        this.fontSize = 18;
        this.lineHeight = new BigDecimal("1.8");
        this.breakIntervals = 15; // Shorter focus periods
        this.timerStyle = "adhd";
        this.animationsEnabled = true; // More visual feedback
    }

    private void applyDyslexiaDefaults() {
        this.fontFamily = "Comic Neue, OpenDyslexic, cursive";
        this.fontSize = 18;
        this.lineHeight = new BigDecimal("2.0");
        this.backgroundColor = "#fffef7"; // Cream background
        this.textColor = "#2c2c2c"; // Softer black
    }

    private void applyAutismDefaults() {
        this.animationsEnabled = false; // Reduce sensory overload
        this.timerStyle = "quiet";
        this.contrastLevel = "high"; // Clear, predictable interface
        this.backgroundColor = "#ffffff";
        this.textColor = "#000000";
    }

    private void applySensoryDefaults() {
        this.backgroundColor = "#f8f9fa"; // Softer background
        this.textColor = "#495057"; // Less harsh contrast
        this.animationsEnabled = false;
        this.contrastLevel = "normal";
        this.breakIntervals = 20; // Longer breaks for sensory regulation
    }

    private void applyADHDTimerDefaults() {
        this.breakIntervals = 15; // Override for combined preset
        this.timerStyle = "adhd";
    }

    private void applyStandardDefaults() {
        this.fontFamily = "Arial, sans-serif";
        this.fontSize = 16;
        this.lineHeight = new BigDecimal("1.5");
        this.backgroundColor = "#ffffff";
        this.textColor = "#333333";
        this.breakIntervals = 25;
        this.timerStyle = "standard";
        this.animationsEnabled = true;
        this.contrastLevel = "normal";
    }

    public boolean isHighContrast() {
        return "high".equals(contrastLevel) || "extra-high".equals(contrastLevel);
    }

    public boolean needsSimplifiedUI() {
        return "adhd".equals(uiPreset) || "dyslexia-adhd".equals(uiPreset);
    }

    public boolean needsDyslexiaSupport() {
        return "dyslexia".equals(uiPreset) || "dyslexia-adhd".equals(uiPreset);
    }

    public boolean needsReducedMotion() {
        return "autism".equals(uiPreset) || "sensory".equals(uiPreset) || !animationsEnabled;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AdaptiveUISettings{" +
                "id=" + id +
                ", userId=" + userId +
                ", uiPreset='" + uiPreset + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontSize=" + fontSize +
                ", breakIntervals=" + breakIntervals +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
