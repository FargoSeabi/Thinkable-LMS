package com.thinkable.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_questions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // 'attention', 'social', 'sensory', 'reading', 'motor'

    @Column(name = "subcategory", length = 50)
    private String subcategory;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", length = 30)
    private String questionType = "likert"; // 'likert', 'binary', 'font_test', 'multiple_choice'

    @Column(name = "options", columnDefinition = "TEXT")
    private String options; // For storing answer options and scoring weights

    @Column(name = "scoring_weight", precision = 3, scale = 2)
    private BigDecimal scoringWeight = new BigDecimal("1.0");

    @Column(name = "age_min")
    private Integer ageMin = 5;

    @Column(name = "age_max")
    private Integer ageMax = 18;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public AssessmentQuestion() {
        this.createdAt = LocalDateTime.now();
    }

    public AssessmentQuestion(String category, String questionText, String questionType) {
        this();
        this.category = category;
        this.questionText = questionText;
        this.questionType = questionType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public BigDecimal getScoringWeight() {
        return scoringWeight;
    }

    public void setScoringWeight(BigDecimal scoringWeight) {
        this.scoringWeight = scoringWeight;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isApplicableForAge(Integer age) {
        if (age == null) return true;
        return age >= ageMin && age <= ageMax;
    }

    public boolean isLikertScale() {
        return "likert".equals(questionType);
    }

    public boolean isBinary() {
        return "binary".equals(questionType);
    }

    public boolean isMultipleChoice() {
        return "multiple_choice".equals(questionType);
    }

    public boolean isFontTest() {
        return "font_test".equals(questionType);
    }

    public int getMaxScore() {
        if (isLikertScale() && options != null && options.has("scale")) {
            return options.get("scale").asInt();
        } else if (isBinary()) {
            return 1;
        }
        return 5; // Default max score
    }

    public double getWeightedScore(int rawScore) {
        return rawScore * scoringWeight.doubleValue();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "AssessmentQuestion{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", questionType='" + questionType + '\'' +
                ", ageMin=" + ageMin +
                ", ageMax=" + ageMax +
                ", active=" + active +
                '}';
    }
}
