package com.thinkable.backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.thinkable.backend.model.Book;
import com.thinkable.backend.model.Question;
import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.repository.QuizRepository;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class AIQuizGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIQuizGenerationService.class);
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;
    
    @Value("${gemini.model:gemini-2.0-flash}")
    private String geminiModel;
    
    private final OkHttpClient httpClient;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    @Autowired
    private PDFTextExtractionService pdfTextExtractionService;
    
    @Autowired
    private QuizRepository quizRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AIQuizGenerationService() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * Generate AI quiz from PDF content
     */
    public GenerationResult generateQuizFromPDF(String fileName, String bookTitle, Book book) {
        try {
            logger.info("Starting AI quiz generation for book: {}", bookTitle);
            logger.info("Gemini API key status - null: {}, empty: {}, length: {}", 
                geminiApiKey == null, 
                geminiApiKey != null ? geminiApiKey.trim().isEmpty() : "N/A",
                geminiApiKey != null ? geminiApiKey.length() : 0);
            
            // Check if Gemini API key is configured
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                logger.warn("Gemini API key not configured, using mock quiz generation");
                return generateMockQuiz(bookTitle, book);
            }
            
            // Extract text from PDF
            PDFTextExtractionService.PDFFullText fullText = pdfTextExtractionService.extractFullText(fileName);
            if (!fullText.isSuccess()) {
                return new GenerationResult(false, "Failed to extract text from PDF: " + fullText.getError(), null);
            }
            
            // Get text content (limit to prevent token overflow)
            String textContent = getTextSample(fullText.getPagesText());
            if (textContent.length() < 100) {
                return new GenerationResult(false, "PDF text too short for quiz generation", null);
            }
            
            // Generate quiz using Gemini
            List<AIQuestion> aiQuestions = generateQuestionsWithGemini(textContent, bookTitle);
            if (aiQuestions == null || aiQuestions.isEmpty()) {
                logger.warn("Gemini generation failed, falling back to mock quiz");
                return generateMockQuiz(bookTitle, book);
            }
            
            // Convert to Quiz entity
            Quiz quiz = createQuizFromAIQuestions(bookTitle, book, aiQuestions);
            Quiz savedQuiz = quizRepository.save(quiz);
            
            logger.info("Successfully generated AI quiz with {} questions for book: {}", aiQuestions.size(), bookTitle);
            return new GenerationResult(true, "Quiz generated successfully", savedQuiz);
            
        } catch (Exception e) {
            logger.error("Error generating AI quiz for book {}: {}", bookTitle, e.getMessage(), e);
            return new GenerationResult(false, "Error generating quiz: " + e.getMessage(), null);
        }
    }
    
    /**
     * Public method to generate questions with text (for TutorContentController)
     */
    public List<AIQuestion> generateQuestionsWithText(String prompt) {
        try {
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                logger.warn("Gemini API key not configured");
                return null;
            }
            
            logger.info("Making Gemini API call with prompt length: {} characters", prompt.length());
            
            // Create Gemini API request body
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
                
            logger.info("Making Gemini API call...");
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    logger.error("Gemini API call failed with status {}: {}", response.code(), errorBody);
                    return null;
                }
                
                String responseBody = response.body().string();
                logger.info("Received Gemini response length: {} characters", responseBody.length());
                
                // Parse Gemini response
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                
                return parseAIResponse(content);
            }
            
        } catch (IOException e) {
            logger.error("Gemini API call failed with IOException: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Gemini API call failed: {}", e.getMessage());
            logger.error("Full error: ", e);
            return null;
        }
    }
    
    /**
     * Generate questions using Gemini API (original method)
     */
    private List<AIQuestion> generateQuestionsWithGemini(String textContent, String bookTitle) {
        try {
            logger.info("Making Gemini API call with key length: {}", geminiApiKey.length());
            
            String prompt = createQuizGenerationPrompt(textContent, bookTitle);
            logger.info("Generated prompt length: {} characters", prompt.length());
            
            // Create Gemini API request body
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
                
            logger.info("Making Gemini API call...");
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    logger.error("Gemini API call failed with status {}: {}", response.code(), errorBody);
                    return null;
                }
                
                String responseBody = response.body().string();
                logger.info("Received Gemini response length: {} characters", responseBody.length());
                
                // Parse Gemini response
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                
                return parseAIResponse(content);
            }
            
        } catch (IOException e) {
            logger.error("Gemini API call failed with IOException: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Gemini API call failed: {}", e.getMessage());
            logger.error("Full error: ", e);
            return null;
        }
    }
    
    /**
     * Create prompt for quiz generation
     */
    private String createQuizGenerationPrompt(String textContent, String bookTitle) {
        return String.format(
            "Based on this text from the book \"%s\", create exactly 5 multiple choice questions.\n\n" +
            "Guidelines:\n" +
            "- Questions should be appropriate for students with learning differences (clear, simple language)\n" +
            "- Focus on key concepts and main ideas\n" +
            "- Avoid trick questions or overly complex wording\n" +
            "- Each question should have 4 answer choices (A, B, C, D)\n" +
            "- Only one correct answer per question\n\n" +
            "Format your response as valid JSON array:\n" +
            "[\n" +
            "    {\n" +
            "        \"question\": \"What is the main topic discussed?\",\n" +
            "        \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
            "        \"correctAnswer\": 0\n" +
            "    }\n" +
            "]\n\n" +
            "Text content:\n%s", 
            bookTitle, textContent.substring(0, Math.min(textContent.length(), 2000)));
    }
    
    /**
     * Parse AI response into question objects
     */
    private List<AIQuestion> parseAIResponse(String response) {
        try {
            // Clean up response (remove code blocks if present)
            String cleanResponse = response.replaceAll("```json", "").replaceAll("```", "").trim();
            
            // Parse JSON array
            return objectMapper.readValue(cleanResponse, new TypeReference<List<AIQuestion>>() {});
            
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert AI questions to Quiz entity
     */
    private Quiz createQuizFromAIQuestions(String bookTitle, Book book, List<AIQuestion> aiQuestions) {
        Quiz quiz = new Quiz();
        quiz.setTitle("AI Quiz: " + bookTitle);
        quiz.setBook(book);
        quiz.setAiGenerated(true);
        
        List<Question> questions = new ArrayList<>();
        for (AIQuestion aiQuestion : aiQuestions) {
            Question question = new Question();
            question.setQuestion(aiQuestion.question);
            question.setOptions(aiQuestion.options);
            question.setCorrectOption(aiQuestion.correctAnswer);
            questions.add(question);
        }
        
        quiz.setQuestions(questions);
        return quiz;
    }
    
    /**
     * Get sample text from PDF (limit size for API)
     */
    private String getTextSample(java.util.Map<Integer, String> allPagesText) {
        StringBuilder sample = new StringBuilder();
        int maxLength = 1500; // Reduced from 3000 to avoid token limits
        
        for (String pageText : allPagesText.values()) {
            if (sample.length() + pageText.length() > maxLength) {
                sample.append(pageText, 0, maxLength - sample.length());
                break;
            }
            sample.append(pageText).append("\n\n");
        }
        
        return sample.toString();
    }
    
    /**
     * Generate mock quiz when OpenAI is not available
     */
    private GenerationResult generateMockQuiz(String bookTitle, Book book) {
        logger.info("Generating mock quiz for book: {}", bookTitle);
        
        Quiz quiz = new Quiz();
        quiz.setTitle("Sample Quiz: " + bookTitle);
        quiz.setBook(book);
        quiz.setAiGenerated(true);
        
        List<Question> questions = new ArrayList<>();
        
        // Mock question 1
        Question q1 = new Question();
        q1.setQuestion("What is the main theme of this book?");
        q1.setOptions(Arrays.asList("Adventure", "Science", "History", "Mystery"));
        q1.setCorrectOption(0);
        questions.add(q1);
        
        // Mock question 2
        Question q2 = new Question();
        q2.setQuestion("This book appears to be written for which audience?");
        q2.setOptions(Arrays.asList("Children", "Adults", "Students", "All ages"));
        q2.setCorrectOption(2);
        questions.add(q2);
        
        // Mock question 3
        Question q3 = new Question();
        q3.setQuestion("What type of document is this?");
        q3.setOptions(Arrays.asList("Novel", "Textbook", "Manual", "Guide"));
        q3.setCorrectOption(1);
        questions.add(q3);
        
        quiz.setQuestions(questions);
        Quiz savedQuiz = quizRepository.save(quiz);
        
        return new GenerationResult(true, "Mock quiz generated successfully (Gemini not configured)", savedQuiz);
    }
    
    /**
     * AI Question structure for parsing
     */
    public static class AIQuestion {
        @JsonProperty("question")
        public String question;
        
        @JsonProperty("options")
        public List<String> options;
        
        @JsonProperty("correctAnswer")
        public Integer correctAnswer;
    }
    
    /**
     * Generation result wrapper
     */
    public static class GenerationResult {
        private final boolean success;
        private final String message;
        private final Quiz quiz;
        
        public GenerationResult(boolean success, String message, Quiz quiz) {
            this.success = success;
            this.message = message;
            this.quiz = quiz;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Quiz getQuiz() { return quiz; }
    }
    
    /**
     * Gemini API request structure
     */
    public static class GeminiRequest {
        @JsonProperty("contents")
        public List<GeminiContent> contents;
        
        public GeminiRequest(List<GeminiContent> contents) {
            this.contents = contents;
        }
    }
    
    /**
     * Gemini API content structure
     */
    public static class GeminiContent {
        @JsonProperty("parts")
        public List<GeminiPart> parts;
        
        public GeminiContent(List<GeminiPart> parts) {
            this.parts = parts;
        }
    }
    
    /**
     * Gemini API part structure
     */
    public static class GeminiPart {
        @JsonProperty("text")
        public String text;
        
        public GeminiPart(String text) {
            this.text = text;
        }
    }
}