package com.thinkable.backend.controller;

import com.thinkable.backend.model.AssessmentQuestion;
import com.thinkable.backend.repository.AssessmentQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @GetMapping("/questions/count")
    public ResponseEntity<Map<String, Object>> getQuestionsCount() {
        long totalCount = assessmentQuestionRepository.count();
        long activeCount = assessmentQuestionRepository.countActiveQuestions();
        
        List<Object[]> categoryCounts = assessmentQuestionRepository.countQuestionsByCategory();
        Map<String, Long> categoryBreakdown = categoryCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        return ResponseEntity.ok(Map.of(
            "totalQuestions", totalCount,
            "activeQuestions", activeCount,
            "byCategory", categoryBreakdown
        ));
    }

    @GetMapping("/questions/all")
    public ResponseEntity<List<AssessmentQuestion>> getAllQuestions() {
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findAllActiveQuestionsOrdered();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/age/{age}")
    public ResponseEntity<Map<String, Object>> getQuestionsForAge(@PathVariable Integer age) {
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findQuestionsForAge(age);
        
        Map<String, Long> categoryBreakdown = questions.stream()
            .collect(Collectors.groupingBy(
                AssessmentQuestion::getCategory,
                Collectors.counting()
            ));

        return ResponseEntity.ok(Map.of(
            "age", age,
            "totalQuestions", questions.size(),
            "byCategory", categoryBreakdown,
            "questions", questions
        ));
    }
}
