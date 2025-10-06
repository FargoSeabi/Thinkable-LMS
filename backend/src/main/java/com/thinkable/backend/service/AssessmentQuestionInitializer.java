package com.thinkable.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkable.backend.model.AssessmentQuestion;
import com.thinkable.backend.repository.AssessmentQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssessmentQuestionInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentQuestionInitializer.class);

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("AssessmentQuestionInitializer starting...");
            
            // Check if we can connect to database
            long count = assessmentQuestionRepository.count();
            logger.info("Current assessment questions count: {}", count);
            
            if (count > 0) {
                logger.info("Assessment questions database already populated with {} questions", count);
                return;
            }

            logger.info("Database is empty - initializing assessment questions...");
            initializeQuestions();
            
            // Verify questions were created
            long finalCount = assessmentQuestionRepository.count();
            logger.info("Assessment questions database initialization complete - created {} questions", finalCount);
            
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to initialize assessment questions", e);
            // Don't rethrow - let app continue to start
        }
    }

    private void initializeQuestions() {
        try {
            List<AssessmentQuestion> questions = new ArrayList<>();

            // ATTENTION/ADHD Questions
            questions.addAll(createAttentionQuestions());
            
            // READING/DYSLEXIA Questions  
            questions.addAll(createReadingQuestions());
            
            // SOCIAL/AUTISM Questions
            questions.addAll(createSocialQuestions());
            
            // SENSORY PROCESSING Questions
            questions.addAll(createSensoryQuestions());
            
            // MOTOR SKILLS Questions
            questions.addAll(createMotorQuestions());
            
            // EMOTIONAL REGULATION Questions
            questions.addAll(createEmotionalQuestions());

            // Save all questions
            assessmentQuestionRepository.saveAll(questions);
            logger.info("Successfully created {} assessment questions", questions.size());

        } catch (Exception e) {
            logger.error("Error initializing assessment questions", e);
        }
    }

    private List<AssessmentQuestion> createAttentionQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("attention", "hyperactivity", 
            "I have difficulty sitting still during lessons or activities", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("attention", "hyperactivity", 
            "I fidget with my hands or feet when I have to sit for a long time", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("attention", "impulsivity", 
            "I blurt out answers before questions are finished", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("attention", "impulsivity", 
            "I have trouble waiting my turn in games or conversations", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("attention", "inattention", 
            "I have trouble paying attention to details in schoolwork", 
            "likert", likertOptions, 1.3, 5, 18));

        questions.add(createQuestion("attention", "inattention", 
            "I often lose things I need for school or activities", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("attention", "inattention", 
            "I get easily distracted by sounds, sights, or thoughts", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("attention", "inattention", 
            "I have difficulty following instructions with multiple steps", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("attention", "executive", 
            "I have trouble organizing my tasks and materials", 
            "likert", likertOptions, 1.2, 8, 18));

        questions.add(createQuestion("attention", "executive", 
            "I avoid or dislike tasks that require sustained mental effort", 
            "likert", likertOptions, 1.1, 8, 18));

        return questions;
    }

    private List<AssessmentQuestion> createReadingQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("reading", "decoding", 
            "I have difficulty sounding out unfamiliar words", 
            "likert", likertOptions, 1.4, 5, 18));

        questions.add(createQuestion("reading", "decoding", 
            "I sometimes read words backwards or mix up similar letters", 
            "likert", likertOptions, 1.3, 5, 18));

        questions.add(createQuestion("reading", "fluency", 
            "I read much slower than others my age", 
            "likert", likertOptions, 1.2, 6, 18));

        questions.add(createQuestion("reading", "fluency", 
            "I lose my place when reading and skip lines", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("reading", "comprehension", 
            "I have trouble understanding what I read, even when I can say the words", 
            "likert", likertOptions, 1.3, 6, 18));

        questions.add(createQuestion("reading", "visual", 
            "Words appear to move, blur, or swim on the page", 
            "likert", likertOptions, 1.4, 5, 18));

        questions.add(createQuestion("reading", "visual", 
            "I get headaches or eye strain when reading", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("reading", "spelling", 
            "I have significant difficulty with spelling, even simple words", 
            "likert", likertOptions, 1.2, 6, 18));

        questions.add(createQuestion("reading", "phonological", 
            "I have trouble hearing the difference between similar sounds", 
            "likert", likertOptions, 1.3, 5, 15));

        questions.add(createQuestion("reading", "memory", 
            "I forget what I just read by the end of a paragraph", 
            "likert", likertOptions, 1.2, 6, 18));

        return questions;
    }

    private List<AssessmentQuestion> createSocialQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("social", "communication", 
            "I have difficulty starting conversations with peers", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("social", "communication", 
            "I find it hard to understand when someone is joking or being sarcastic", 
            "likert", likertOptions, 1.3, 8, 18));

        questions.add(createQuestion("social", "interaction", 
            "I prefer to play or work alone rather than with others", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("social", "interaction", 
            "I have trouble making friends or keeping friendships", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("social", "nonverbal", 
            "I have difficulty understanding facial expressions and body language", 
            "likert", likertOptions, 1.3, 5, 18));

        questions.add(createQuestion("social", "nonverbal", 
            "I avoid eye contact when talking to people", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("social", "routine", 
            "I get very upset when my daily routine is changed unexpectedly", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("social", "routine", 
            "I have very specific interests that I focus on intensely", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("social", "perspective", 
            "I find it difficult to understand other people's thoughts and feelings", 
            "likert", likertOptions, 1.3, 8, 18));

        questions.add(createQuestion("social", "flexibility", 
            "I prefer things to be done the same way every time", 
            "likert", likertOptions, 1.1, 5, 18));

        return questions;
    }

    private List<AssessmentQuestion> createSensoryQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("sensory", "auditory", 
            "I am bothered by everyday sounds that don't seem to bother others", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("sensory", "auditory", 
            "I cover my ears or get upset in noisy environments", 
            "likert", likertOptions, 1.3, 5, 18));

        questions.add(createQuestion("sensory", "visual", 
            "Bright lights or fluorescent lighting bothers me", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("sensory", "visual", 
            "I am sensitive to visual clutter and busy patterns", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("sensory", "tactile", 
            "I am very sensitive to clothing textures and tags", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("sensory", "tactile", 
            "I dislike being touched or hugged, even by family", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("sensory", "proprioception", 
            "I seem clumsy and bump into things frequently", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("sensory", "vestibular", 
            "I get motion sickness easily or dislike swinging/spinning", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("sensory", "processing", 
            "I become overwhelmed in busy or crowded environments", 
            "likert", likertOptions, 1.3, 5, 18));

        questions.add(createQuestion("sensory", "integration", 
            "I have difficulty filtering out background noise to focus on important sounds", 
            "likert", likertOptions, 1.2, 5, 18));

        return questions;
    }

    private List<AssessmentQuestion> createMotorQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("motor", "fine_motor", 
            "I have difficulty with handwriting and my writing is messy", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("motor", "fine_motor", 
            "I struggle with tasks like tying shoes, buttoning clothes, or using utensils", 
            "likert", likertOptions, 1.1, 5, 15));

        questions.add(createQuestion("motor", "gross_motor", 
            "I have trouble with sports or physical activities that require coordination", 
            "likert", likertOptions, 1.0, 5, 18));

        questions.add(createQuestion("motor", "planning", 
            "I have difficulty learning new physical skills or movements", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("motor", "bilateral", 
            "I have trouble using both hands together for tasks", 
            "likert", likertOptions, 1.0, 5, 18));

        return questions;
    }

    private List<AssessmentQuestion> createEmotionalQuestions() throws Exception {
        List<AssessmentQuestion> questions = new ArrayList<>();
        JsonNode likertOptions = objectMapper.readTree(
            "{\"scale\": 5, \"labels\": [\"Never\", \"Rarely\", \"Sometimes\", \"Often\", \"Always\"], \"scores\": [1, 2, 3, 4, 5]}"
        );

        questions.add(createQuestion("emotional", "regulation", 
            "I have intense emotional reactions that seem bigger than the situation", 
            "likert", likertOptions, 1.2, 5, 18));

        questions.add(createQuestion("emotional", "regulation", 
            "I have difficulty calming down when I'm upset", 
            "likert", likertOptions, 1.1, 5, 18));

        questions.add(createQuestion("emotional", "anxiety", 
            "I worry excessively about school performance or social situations", 
            "likert", likertOptions, 1.0, 8, 18));

        questions.add(createQuestion("emotional", "self_esteem", 
            "I often feel different from other students my age", 
            "likert", likertOptions, 1.1, 8, 18));

        questions.add(createQuestion("emotional", "frustration", 
            "I get frustrated easily when tasks are difficult", 
            "likert", likertOptions, 1.1, 5, 18));

        return questions;
    }

    private AssessmentQuestion createQuestion(String category, String subcategory, String questionText, 
                                            String questionType, JsonNode options, double weight, 
                                            int ageMin, int ageMax) {
        AssessmentQuestion question = new AssessmentQuestion();
        question.setCategory(category);
        question.setSubcategory(subcategory);
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setOptions(options.toString());
        question.setScoringWeight(new BigDecimal(String.valueOf(weight)));
        question.setAgeMin(ageMin);
        question.setAgeMax(ageMax);
        question.setActive(true);
        return question;
    }
}
