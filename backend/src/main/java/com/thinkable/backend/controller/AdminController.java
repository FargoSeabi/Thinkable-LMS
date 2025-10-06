package com.thinkable.backend.controller;

import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thinkable.backend.model.Book;
import com.thinkable.backend.model.Lesson;
import com.thinkable.backend.model.Question;
import com.thinkable.backend.model.Quiz;
import com.thinkable.backend.model.User;
import com.thinkable.backend.entity.LearningContent;
import com.thinkable.backend.entity.ContentAccessibilityTag;
import com.thinkable.backend.entity.StudentContentInteraction;
import com.thinkable.backend.repository.LearningContentRepository;
import com.thinkable.backend.repository.ContentAccessibilityTagRepository;
import com.thinkable.backend.repository.StudentContentInteractionRepository;
import com.thinkable.backend.repository.BookRepository;
import com.thinkable.backend.repository.LeaderboardRepository;
import com.thinkable.backend.repository.LessonProgressRepository;
import com.thinkable.backend.repository.LessonRepository;
import com.thinkable.backend.repository.QuestionRepository;
import com.thinkable.backend.repository.QuizRepository;
import com.thinkable.backend.repository.UserRepository;
import com.thinkable.backend.repository.AssessmentQuestionRepository;
import com.thinkable.backend.service.AIQuizGenerationService;

@RestController
@RequestMapping("/api/admin")
public class AdminController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

//    @Value("${file.upload-dir}")
//    private String uploadDir;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    @Autowired
    private LessonProgressRepository lessonProgressRepository;
    
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AIQuizGenerationService aiQuizGenerationService;
    
    @Autowired
    private LearningContentRepository learningContentRepository;
    
    @Autowired
    private ContentAccessibilityTagRepository contentAccessibilityTagRepository;
    
    @Autowired
    private StudentContentInteractionRepository studentContentInteractionRepository;
    
    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            LOGGER.error("Error fetching users: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching users"));
        }
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody UserRequest request) {
        try {
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            if (userRepository.findByEmail(request.email) != null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
            }
            User user = new User();
            user.setName(request.name);
            user.setEmail(request.email);
            user.setPassword(request.password);
            user.setRole("STUDENT");
            user.setUsername(request.email.split("@")[0].replaceAll("[^a-zA-Z0-9]", ""));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(new SuccessResponse("Student added successfully"));
        } catch (Exception e) {
            LOGGER.error("Error adding user: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error adding user"));
        }
    }

    @PutMapping("/update-user")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        try {
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            User user = userRepository.findByEmail(request.email);
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }
            user.setName(request.name);
            userRepository.save(user);
            return ResponseEntity.ok(new SuccessResponse("User updated successfully"));
        } catch (Exception e) {
            LOGGER.error("Error updating user: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error updating user"));
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        try {
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            User user = userRepository.findByEmail(request.email);
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }
            user.setPassword(request.password);
            userRepository.save(user);
            return ResponseEntity.ok(new SuccessResponse("Password updated successfully"));
        } catch (Exception e) {
            LOGGER.error("Error updating password: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error updating password"));
        }
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestBody DeleteUserRequest request) {
        try {
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            User user = userRepository.findByEmail(request.email);
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }
            
            // Prevent admin from deleting themselves
            if (user.getId().equals(admin.getId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Cannot delete yourself"));
            }
            
            // Delete related records first to avoid foreign key constraint violations
            LOGGER.info("Deleting user {} - cleaning up related records", user.getEmail());
            
            // Delete leaderboard entries
            leaderboardRepository.deleteByUserId(user.getId());
            LOGGER.info("Deleted leaderboard entries for user {}", user.getEmail());
            
            // Delete lesson progress entries
            lessonProgressRepository.deleteByUserId(user.getId());
            LOGGER.info("Deleted lesson progress entries for user {}", user.getEmail());
            
            // Now delete the user
            userRepository.delete(user);
            LOGGER.info("Successfully deleted user {}", user.getEmail());
            
            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (Exception e) {
            LOGGER.error("Error deleting user: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error deleting user: " + e.getMessage()));
        }
    }

    @GetMapping("/lessons")
    public ResponseEntity<?> getLessons(@RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            List<Lesson> lessons = lessonRepository.findAll();
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            LOGGER.error("Error fetching lessons: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching lessons"));
        }
    }

    @PostMapping("/add-lesson")
    public ResponseEntity<?> addLesson(@RequestBody LessonRequest request) {
        try {
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            Lesson lesson = new Lesson();
            lesson.setTitle(request.title);
            lesson.setDescription(request.description);
            lesson.setYoutubeUrl(request.youtubeUrl);
            lesson.setCreatedAt(LocalDateTime.now());
            lessonRepository.save(lesson);
            return ResponseEntity.ok(new SuccessResponse("Lesson added successfully"));
        } catch (Exception e) {
            LOGGER.error("Error adding lesson: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error adding lesson"));
        }
    }

    @DeleteMapping("/delete-lesson/{lessonId}")
    public ResponseEntity<?> deleteLesson(@PathVariable Long lessonId, @RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
            lessonRepository.delete(lesson);
            return ResponseEntity.ok(new SuccessResponse("Lesson deleted successfully"));
        } catch (Exception e) {
            LOGGER.error("Error deleting lesson: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error deleting lesson"));
        }
    }

    @PostMapping("/add-quiz")
    public ResponseEntity<?> addQuiz(@RequestBody QuizRequest request) {
        try {
            LOGGER.info("Received quiz creation request: adminEmail={}, lessonId={}, bookId={}, title={}", 
                        request.adminEmail, request.lessonId, request.bookId, request.title);
            
            // Validate request
            if (request.adminEmail == null || request.adminEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Admin email is required"));
            }
            if (request.lessonId == null && request.bookId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Either Lesson ID or Book ID is required"));
            }
            if (request.lessonId != null && request.bookId != null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Quiz can only be associated with either a lesson or a book, not both"));
            }
            if (request.title == null || request.title.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Quiz title is required"));
            }
            if (request.questions == null || request.questions.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("At least one question is required"));
            }

            // Verify admin
            User admin = userRepository.findByEmail(request.adminEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                LOGGER.warn("Unauthorized access attempt by: {}", request.adminEmail);
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            LOGGER.info("Admin verified: {}", request.adminEmail);

            // Create quiz
            Quiz quiz = new Quiz();
            quiz.setTitle(request.title);
            
            // Set either lesson or book
            if (request.lessonId != null) {
                Lesson lesson = lessonRepository.findById(request.lessonId)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
                quiz.setLesson(lesson);
                LOGGER.info("Lesson found: id={}", lesson.getId());
            } else {
                Book book = bookRepository.findById(request.bookId)
                        .orElseThrow(() -> new IllegalArgumentException("Book not found"));
                quiz.setBook(book);
                LOGGER.info("Book found: id={}", book.getId());
            }
            
            List<Question> questions = new ArrayList<>();

            // Validate and create questions
            for (QuizRequest.QuestionRequest qr : request.questions) {
                if (qr.question == null || qr.question.isEmpty()) {
                    throw new IllegalArgumentException("Question text cannot be empty");
                }
                if (qr.options == null || qr.options.isEmpty()) {
                    throw new IllegalArgumentException("Options cannot be empty");
                }
                if (qr.correctOption == null || qr.correctOption < 0 || qr.correctOption >= qr.options.size()) {
                    throw new IllegalArgumentException("Invalid correct option index");
                }
                Question question = new Question();
                question.setQuestion(qr.question);
                question.setOptions(qr.options);
                question.setCorrectOption(qr.correctOption);
                question.setQuiz(quiz); // Explicitly set the quiz reference
                questions.add(question);
            }

            quiz.setQuestions(questions);
            LOGGER.info("Saving quiz with title: {} and {} questions", quiz.getTitle(), questions.size());
            quizRepository.save(quiz);
            LOGGER.info("Quiz saved successfully for lessonId: {}", request.lessonId);
            return ResponseEntity.ok(new SuccessResponse("Quiz added successfully"));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Validation error adding quiz: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Error adding quiz: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error adding quiz: " + e.getMessage()));
        }
    }

    @GetMapping("/books")
    public ResponseEntity<?> getBooks(@RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            List<Book> books = bookRepository.findAll();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            LOGGER.error("Error fetching books: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching books"));
        }
    }

    @Value("${app.uploads.dir}")
    private String uploadBaseDir; // Defined in application.properties

    @PostMapping("/add-book")
    public ResponseEntity<?> addBook(
            @RequestParam String adminEmail,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam("pdf") MultipartFile pdfFile) {

        try {
            // 1. Admin validation
            User admin = userRepository.findByEmail(adminEmail.trim());
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Admin privileges required"));
            }

            // 2. Input validation
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Title is required"));
            }
            if (author == null || author.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Author is required"));
            }

            // 3. File validation
            if (pdfFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("PDF file is required"));
            }
            if (pdfFile.getSize() > 20 * 1024 * 1024) { // 20MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ErrorResponse("File size exceeds 20MB limit"));
            }
            if (!"application/pdf".equals(pdfFile.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Only PDF files are allowed"));
            }

            // 4. Secure file handling
            Path uploadDir = Paths.get(uploadBaseDir, "books").normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 5. Generate secure filename
            String originalFilename = StringUtils.cleanPath(
                    Objects.requireNonNull(pdfFile.getOriginalFilename()));
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadDir.resolve(fileName).normalize();

            // 6. Validate path security
            if (!filePath.startsWith(uploadDir.normalize())) {
                throw new IOException("Invalid file path");
            }

            // 7. Save file
            try (InputStream inputStream = pdfFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 8. Save book record
            Book book = new Book();
            book.setTitle(title.trim());
            book.setAuthor(author.trim());
            book.setPdfUrl("/books/" + fileName); // Relative URL
            book.setCreatedAt(LocalDateTime.now());
            bookRepository.save(book);

            return ResponseEntity.ok(new SuccessResponse("Book added successfully"));

        } catch (IOException e) {
            LOGGER.error("File processing error: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to process file"));
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Operation failed"));
        }
    }

    @DeleteMapping("/delete-book/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId, @RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            
            // Delete related records first to avoid foreign key constraint violations
            LOGGER.info("Deleting book {} - cleaning up related records", book.getTitle());
            
            // Delete questions first (they reference quizzes)
            questionRepository.deleteByBookId(book.getId());
            LOGGER.info("Deleted questions for book {}", book.getTitle());
            
            // Then delete quizzes associated with this book
            quizRepository.deleteByBookId(book.getId());
            LOGGER.info("Deleted quizzes for book {}", book.getTitle());
            
            // Delete the PDF file
            Files.deleteIfExists(Paths.get(book.getPdfUrl()));
            
            // Now delete the book
            bookRepository.delete(book);
            LOGGER.info("Successfully deleted book {}", book.getTitle());
            
            return ResponseEntity.ok(new SuccessResponse("Book deleted successfully"));
        } catch (IOException e) {
            LOGGER.error("Error deleting book file: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error deleting book file"));
        } catch (Exception e) {
            LOGGER.error("Error deleting book: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error deleting book: " + e.getMessage()));
        }
    }

    @PostMapping("/generate-quiz/{bookId}")
    public ResponseEntity<?> generateAIQuiz(@PathVariable Long bookId, @RequestParam String email) {
        try {
            // Verify admin access
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }

            // Find the book
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Book not found"));
            }

            // Check if quiz already exists for this book
            List<Quiz> existingQuizzes = quizRepository.findByBook(book);
            if (!existingQuizzes.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Quiz already exists for this book"));
            }

            // Extract filename from PDF URL
            String pdfUrl = book.getPdfUrl();
            String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);

            LOGGER.info("Generating AI quiz for book: {} ({})", book.getTitle(), fileName);

            // Generate quiz using AI service
            AIQuizGenerationService.GenerationResult result = 
                aiQuizGenerationService.generateQuizFromPDF(fileName, book.getTitle(), book);

            if (result.isSuccess()) {
                return ResponseEntity.ok(new QuizGenerationResponse(
                    result.getMessage(), 
                    result.getQuiz().getId(),
                    result.getQuiz().getQuestions().size()
                ));
            } else {
                return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Quiz generation failed: " + result.getMessage()));
            }

        } catch (Exception e) {
            LOGGER.error("Error generating AI quiz for book {}: {}", bookId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to generate quiz: " + e.getMessage()));
        }
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getQuizById(@PathVariable Long quizId) {
        try {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            LOGGER.error("Error retrieving quiz {}: {}", quizId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to retrieve quiz: " + e.getMessage()));
        }
    }

    @GetMapping("/quiz/book/{bookId}")
    public ResponseEntity<?> getQuizByBookId(@PathVariable Long bookId) {
        try {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Quiz> quizzes = quizRepository.findByBook(book);
            if (quizzes.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Return the first quiz found (should only be one per book)
            return ResponseEntity.ok(quizzes.get(0));
        } catch (Exception e) {
            LOGGER.error("Error retrieving quiz for book {}: {}", bookId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to retrieve quiz: " + e.getMessage()));
        }
    }

    @GetMapping("/quizzes")
    public ResponseEntity<?> getAllQuizzes(@RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            List<Quiz> quizzes = quizRepository.findAll();
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            LOGGER.error("Error fetching quizzes: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching quizzes"));
        }
    }

    @DeleteMapping("/delete-quiz/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId, @RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
            quizRepository.delete(quiz);
            return ResponseEntity.ok(new SuccessResponse("Quiz deleted successfully"));
        } catch (Exception e) {
            LOGGER.error("Error deleting quiz: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error deleting quiz"));
        }
    }

    @GetMapping("/assessment-questions")
    public ResponseEntity<?> getAssessmentQuestionsInfo(@RequestParam String email) {
        try {
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }
            
            long totalCount = assessmentQuestionRepository.count();
            return ResponseEntity.ok(Map.of(
                "totalQuestions", totalCount,
                "message", totalCount > 0 ? "Assessment questions are populated" : "No assessment questions found"
            ));
        } catch (Exception e) {
            LOGGER.error("Error checking assessment questions: ", e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error checking assessment questions"));
        }
    }

    /**
     * Clear all discovery learning content - DANGER: This will delete everything!
     * This includes all learning content, uploaded files, quizzes, interactions, and reviews
     */
    @DeleteMapping("/clear-all-content")
    public ResponseEntity<?> clearAllContent(@RequestParam String email) {
        try {
            // Verify admin authorization
            User admin = userRepository.findByEmail(email);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized: Admin access required"));
            }

            LOGGER.warn("ADMIN {} is clearing ALL discovery learning content - this is irreversible!", admin.getEmail());
            
            int deletedFiles = 0;
            int deletedContent = 0;
            int deletedQuizzes = 0;
            int deletedInteractions = 0;
            int deletedTags = 0;
            
            // Get all learning content first (so we can delete files)
            List<LearningContent> allContent = learningContentRepository.findAll();
            
            LOGGER.info("Found {} learning content items to delete", allContent.size());
            
            // Delete physical files from storage
            for (LearningContent content : allContent) {
                if (content.getFilePath() != null) {
                    try {
                        Path filePath = Paths.get("uploads/content/" + content.getFilePath());
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            deletedFiles++;
                            LOGGER.info("Deleted file: {}", filePath);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to delete file: {}", content.getFilePath(), e);
                    }
                }
            }
            
            // Delete all quizzes related to learning content
            List<Quiz> contentQuizzes = quizRepository.findAll().stream()
                    .filter(quiz -> quiz.getLearningContentId() != null)
                    .collect(Collectors.toList());
            
            for (Quiz quiz : contentQuizzes) {
                quizRepository.delete(quiz);
                deletedQuizzes++;
            }
            
            // Delete all student interactions with content
            long interactionCount = studentContentInteractionRepository.count();
            studentContentInteractionRepository.deleteAll();
            deletedInteractions = (int) interactionCount;
            
            // Delete all accessibility tags
            long tagCount = contentAccessibilityTagRepository.count();
            contentAccessibilityTagRepository.deleteAll();
            deletedTags = (int) tagCount;
            
            // Finally, delete all learning content from database
            deletedContent = allContent.size();
            learningContentRepository.deleteAll();
            
            // Also clean up orphaned directories
            try {
                Path uploadsPath = Paths.get("uploads/content");
                if (Files.exists(uploadsPath)) {
                    Files.walk(uploadsPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                                LOGGER.info("Cleaned up orphaned file: {}", file);
                            } catch (IOException e) {
                                LOGGER.error("Failed to delete orphaned file: {}", file, e);
                            }
                        });
                }
            } catch (IOException e) {
                LOGGER.error("Failed to clean up uploads directory", e);
            }
            
            String message = String.format(
                "All discovery learning content cleared successfully! " +
                "Deleted: %d content items, %d files, %d quizzes, %d interactions, %d tags",
                deletedContent, deletedFiles, deletedQuizzes, deletedInteractions, deletedTags
            );
            
            LOGGER.warn("CONTENT CLEAR COMPLETE: {}", message);
            
            return ResponseEntity.ok(new ContentClearResponse(
                message, deletedContent, deletedFiles, deletedQuizzes, deletedInteractions, deletedTags
            ));
            
        } catch (Exception e) {
            LOGGER.error("Error clearing all content: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error clearing content: " + e.getMessage()));
        }
    }

    public static class UserRequest {
        public String adminEmail;
        public String name;
        public String email;
        public String password;
    }

    public static class UpdateUserRequest {
        public String adminEmail;
        public String email;
        public String name;
    }

    public static class UpdatePasswordRequest {
        public String adminEmail;
        public String email;
        public String password;
    }

    public static class DeleteUserRequest {
        public String adminEmail;
        public String email;
    }

    public static class LessonRequest {
        public String adminEmail;
        public String title;
        public String description;
        public String youtubeUrl;
    }

    public static class QuizRequest {
        public String adminEmail;
        public Long lessonId;
        public Long bookId;
        public String title;
        public List<QuestionRequest> questions;

        public static class QuestionRequest {
            public String question;
            public List<String> options;
            public Integer correctOption;
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    public static class QuizGenerationResponse {
        public String message;
        public Long quizId;
        public int questionCount;

        public QuizGenerationResponse(String message, Long quizId, int questionCount) {
            this.message = message;
            this.quizId = quizId;
            this.questionCount = questionCount;
        }
    }
    
    public static class ContentClearResponse {
        public String message;
        public int deletedContent;
        public int deletedFiles;
        public int deletedQuizzes;
        public int deletedInteractions;
        public int deletedTags;

        public ContentClearResponse(String message, int deletedContent, int deletedFiles, 
                                   int deletedQuizzes, int deletedInteractions, int deletedTags) {
            this.message = message;
            this.deletedContent = deletedContent;
            this.deletedFiles = deletedFiles;
            this.deletedQuizzes = deletedQuizzes;
            this.deletedInteractions = deletedInteractions;
            this.deletedTags = deletedTags;
        }
    }
}
