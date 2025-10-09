package com.thinkable.backend.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkable.backend.entity.LearningContent;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AI Service for Smart Notes functionality
 * Provides AI-powered study suggestions, Q&A, and note enhancement
 */
@Service
public class SmartNotesAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartNotesAIService.class);
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;
    
    private final OkHttpClient httpClient;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private TextExtractionService textExtractionService;
    
    public SmartNotesAIService() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * Generate study suggestions based on content and existing notes
     */
    public AIResponse generateStudySuggestions(LearningContent content, String existingNotes) {
        if (!isApiKeyConfigured()) {
            return createMockSuggestions();
        }
        
        try {
            String contentText = extractContentText(content);
            String prompt = createStudySuggestionsPrompt(content, contentText, existingNotes);
            
            return callGeminiAPI(prompt);
            
        } catch (Exception e) {
            logger.error("Error generating study suggestions: {}", e.getMessage());
            return new AIResponse(false, "Error generating suggestions", null);
        }
    }
    
    /**
     * Answer student questions about the content
     */
    public AIResponse answerQuestion(LearningContent content, String question, String existingNotes) {
        if (!isApiKeyConfigured()) {
            return createMockAnswer(question);
        }
        
        try {
            String contentText = extractContentText(content);
            String prompt = createQuestionAnswerPrompt(content, contentText, question, existingNotes);
            
            return callGeminiAPI(prompt);
            
        } catch (Exception e) {
            logger.error("Error answering question: {}", e.getMessage());
            return new AIResponse(false, "Error generating answer", null);
        }
    }
    
    /**
     * Suggest improvements to existing notes
     */
    public AIResponse suggestNoteImprovements(LearningContent content, String notes) {
        if (!isApiKeyConfigured()) {
            return createMockImprovements();
        }
        
        try {
            String contentText = extractContentText(content);
            String prompt = createNoteImprovementPrompt(content, contentText, notes);
            
            return callGeminiAPI(prompt);
            
        } catch (Exception e) {
            logger.error("Error suggesting note improvements: {}", e.getMessage());
            return new AIResponse(false, "Error generating suggestions", null);
        }
    }
    
    /**
     * Check if Gemini API key is configured
     */
    private boolean isApiKeyConfigured() {
        return geminiApiKey != null && !geminiApiKey.trim().isEmpty();
    }
    
    /**
     * Extract text content from learning material
     */
    private String extractContentText(LearningContent content) {
        try {
            Map<String, Object> extractionResult = textExtractionService.extractTextFromContent(content, new HashMap<>());
            if (extractionResult.containsKey("text")) {
                String extractedText = (String) extractionResult.get("text");
                if (extractedText != null && !extractedText.trim().isEmpty()) {
                    // Limit text length to avoid token limits
                    return extractedText.length() > 2000 ? extractedText.substring(0, 2000) : extractedText;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract text from content {}: {}", content.getFileName(), e.getMessage());
        }
        
        // Fallback to title and description
        return content.getTitle() + "\n\n" + (content.getDescription() != null ? content.getDescription() : "");
    }
    
    /**
     * Create prompt for study suggestions
     */
    private String createStudySuggestionsPrompt(LearningContent content, String contentText, String existingNotes) {
        return String.format(
            "You are a helpful AI study assistant for students with learning differences. " +
            "Based on the learning content and the student's existing notes, provide helpful study suggestions.\n\n" +
            "Content Title: %s\n" +
            "Subject: %s\n" +
            "Difficulty: %s\n\n" +
            "Content Text (first 2000 chars):\n%s\n\n" +
            "Student's Current Notes:\n%s\n\n" +
            "Provide 2-3 specific, actionable study suggestions that are:\n" +
            "- Clear and easy to understand\n" +
            "- Helpful for students with ADHD, dyslexia, or autism\n" +
            "- Focus on key concepts and practical learning strategies\n" +
            "- Encourage active learning\n\n" +
            "Format your response as simple, numbered suggestions. Keep each suggestion to 1-2 sentences.\n" +
            "Do not include JSON formatting - just plain text numbered suggestions.", 
            content.getTitle(), 
            content.getSubjectArea(), 
            content.getDifficultyLevel(),
            contentText,
            existingNotes != null && !existingNotes.trim().isEmpty() ? existingNotes : "No notes yet"
        );
    }
    
    /**
     * Create prompt for answering questions
     */
    private String createQuestionAnswerPrompt(LearningContent content, String contentText, String question, String existingNotes) {
        return String.format(
            "You are a helpful AI tutor for students with learning differences." +
            "A student is studying the following content and has asked a question.\n\n" +
            "Content: %s\n" +
            "Subject: %s\n\n" +
            "Content Text:\n%s\n\n" +
            "Student's Notes:\n%s\n\n" +
            "Student's Question: %s\n\n" +
            "Provide a clear, helpful answer that is:\n" +
            "- Easy to understand for students with learning differences\n" +
            "- Directly addresses their question\n" +
            "- Relates to the content they're studying\n" +
            "- Includes examples if helpful\n" +
            "- Encouraging and supportive\n\n" +
            "Keep your answer concise (2-3 sentences) and avoid complex terminology.", 
            content.getTitle(), 
            content.getSubjectArea(), 
            contentText,
            existingNotes != null && !existingNotes.trim().isEmpty() ? existingNotes : "No notes yet",
            question
        );
    }
    
    /**
     * Create prompt for note improvement suggestions
     */
    private String createNoteImprovementPrompt(LearningContent content, String contentText, String notes) {
        return String.format(
            "You are a helpful AI study assistant. A student has taken notes while studying, " +
            "and you should suggest improvements to make their notes more effective for learning.\n\n" +
            "Content: %s\n" +
            "Subject: %s\n\n" +
            "Content Summary:\n%s\n\n" +
            "Student's Current Notes:\n%s\n\n" +
            "Suggest 1-2 ways the student could improve their notes, such as:\n" +
            "- Adding key concepts they might have missed\n" +
            "- Better organization or structure\n" +
            "- Including examples or connections\n" +
            "- Adding questions for self-testing\n\n" +
            "Be encouraging and specific. Keep suggestions brief and actionable.", 
            content.getTitle(), 
            content.getSubjectArea(), 
            contentText,
            notes
        );
    }
    
    /**
     * Make API call to Gemini
     */
    private AIResponse callGeminiAPI(String prompt) {
        try {
            // Create request body using existing structure from AIQuizGenerationService
            String requestBody = objectMapper.writeValueAsString(
                new GeminiRequest(
                    Arrays.asList(
                        new GeminiContent(
                            Arrays.asList(
                                new GeminiPart(prompt)
                            )
                        )
                    )
                )
            );
            
            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;
            Request request = new Request.Builder()
                .url(urlWithKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, JSON))
                .build();
            
            logger.info("Making Gemini API call for smart notes...");
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    logger.error("Gemini API call failed with status {}: {}", response.code(), errorBody);
                    return new AIResponse(false, "AI service temporarily unavailable", null);
                }
                
                String responseBody = response.body().string();
                
                // Parse response
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                
                return new AIResponse(true, "Success", content.trim());
            }
            
        } catch (IOException e) {
            logger.error("Gemini API call failed: {}", e.getMessage());
            return new AIResponse(false, "Network error", null);
        } catch (Exception e) {
            logger.error("Unexpected error in Gemini API call: {}", e.getMessage());
            return new AIResponse(false, "Service error", null);
        }
    }
    
    /**
     * Create mock suggestions when API is not available
     */
    private AIResponse createMockSuggestions() {
        String mockSuggestions = 
            "1. Try breaking down the main concepts into smaller, manageable chunks and create visual mind maps to see connections.\n\n" +
            "2. After reading each section, write a quick summary in your own words to check your understanding.\n\n" +
            "3. Create flashcards for key terms and review them regularly using spaced repetition.";
        return new AIResponse(true, "Mock suggestions (Gemini not configured)", mockSuggestions);
    }
    
    /**
     * Create mock answer when API is not available
     */
    private AIResponse createMockAnswer(String question) {
        String mockAnswer = String.format(
            "I understand you're asking about: \"%s\"\n\n" +
            "While I can't access the full AI features right now, I'd suggest reviewing your notes and the main content sections. " +
            "Try breaking down your question into smaller parts, and look for key terms in the material.", question);
        return new AIResponse(true, "Mock answer (Gemini not configured)", mockAnswer);
    }
    
    /**
     * Create mock improvement suggestions when API is not available
     */
    private AIResponse createMockImprovements() {
        String mockImprovements = 
            "1. Consider adding more specific examples or connections to help remember the concepts.\n\n" +
            "2. Try organizing your notes with clear headings and bullet points for easier review.";
        return new AIResponse(true, "Mock improvements (Gemini not configured)", mockImprovements);
    }
    
    // Reuse existing Gemini API structures from AIQuizGenerationService
    public static class GeminiRequest {
        @JsonProperty("contents")
        public java.util.List<GeminiContent> contents;
        
        public GeminiRequest(java.util.List<GeminiContent> contents) {
            this.contents = contents;
        }
    }
    
    public static class GeminiContent {
        @JsonProperty("parts")
        public java.util.List<GeminiPart> parts;
        
        public GeminiContent(java.util.List<GeminiPart> parts) {
            this.parts = parts;
        }
    }
    
    public static class GeminiPart {
        @JsonProperty("text")
        public String text;
        
        public GeminiPart(String text) {
            this.text = text;
        }
    }
    
    /**
     * AI Response wrapper
     */
    public static class AIResponse {
        private final boolean success;
        private final String message;
        private final String content;
        
        public AIResponse(boolean success, String message, String content) {
            this.success = success;
            this.message = message;
            this.content = content;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getContent() { return content; }
    }
}