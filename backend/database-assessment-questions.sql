-- Assessment Questions Database for ThinkAble
-- Research-based questionnaire for identifying neurodivergent learning needs
-- Based on validated assessment tools like ADOS, ADHD-RS, and dyslexia screening

-- Clear existing data
DELETE FROM assessment_questions;

-- ATTENTION/ADHD Questions (Category: attention)
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('attention', 'hyperactivity', 'I have difficulty sitting still during lessons or activities', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('attention', 'hyperactivity', 'I fidget with my hands or feet when I have to sit for a long time', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('attention', 'impulsivity', 'I blurt out answers before questions are finished', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('attention', 'impulsivity', 'I have trouble waiting my turn in games or conversations', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('attention', 'inattention', 'I have trouble paying attention to details in schoolwork', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 18, true),
('attention', 'inattention', 'I often lose things I need for school or activities', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('attention', 'inattention', 'I get easily distracted by sounds, sights, or thoughts', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('attention', 'inattention', 'I have difficulty following instructions with multiple steps', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('attention', 'executive', 'I have trouble organizing my tasks and materials', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 8, 18, true),
('attention', 'executive', 'I avoid or dislike tasks that require sustained mental effort', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 8, 18, true),

-- READING/DYSLEXIA Questions (Category: reading) 
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('reading', 'decoding', 'I have difficulty sounding out unfamiliar words', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.4, 5, 18, true),
('reading', 'decoding', 'I sometimes read words backwards or mix up similar letters', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 18, true),
('reading', 'fluency', 'I read much slower than others my age', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 6, 18, true),
('reading', 'fluency', 'I lose my place when reading and skip lines', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('reading', 'comprehension', 'I have trouble understanding what I read, even when I can say the words', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 6, 18, true),
('reading', 'visual', 'Words appear to move, blur, or swim on the page', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.4, 5, 18, true),
('reading', 'visual', 'I get headaches or eye strain when reading', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('reading', 'spelling', 'I have significant difficulty with spelling, even simple words', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 6, 18, true),
('reading', 'phonological', 'I have trouble hearing the difference between similar sounds', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 15, true),
('reading', 'memory', 'I forget what I just read by the end of a paragraph', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 6, 18, true),

-- SOCIAL/AUTISM Questions (Category: social)
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('social', 'communication', 'I have difficulty starting conversations with peers', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('social', 'communication', 'I find it hard to understand when someone is joking or being sarcastic', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 8, 18, true),
('social', 'interaction', 'I prefer to play or work alone rather than with others', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('social', 'interaction', 'I have trouble making friends or keeping friendships', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('social', 'nonverbal', 'I have difficulty understanding facial expressions and body language', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 18, true),
('social', 'nonverbal', 'I avoid eye contact when talking to people', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('social', 'routine', 'I get very upset when my daily routine is changed unexpectedly', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('social', 'routine', 'I have very specific interests that I focus on intensely', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('social', 'perspective', 'I find it difficult to understand other people''s thoughts and feelings', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 8, 18, true),
('social', 'flexibility', 'I prefer things to be done the same way every time', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),

-- SENSORY PROCESSING Questions (Category: sensory)
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('sensory', 'auditory', 'I am bothered by everyday sounds that don''t seem to bother others', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('sensory', 'auditory', 'I cover my ears or get upset in noisy environments', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 18, true),
('sensory', 'visual', 'Bright lights or fluorescent lighting bothers me', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('sensory', 'visual', 'I am sensitive to visual clutter and busy patterns', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('sensory', 'tactile', 'I am very sensitive to clothing textures and tags', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('sensory', 'tactile', 'I dislike being touched or hugged, even by family', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('sensory', 'proprioception', 'I seem clumsy and bump into things frequently', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('sensory', 'vestibular', 'I get motion sickness easily or dislike swinging/spinning', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('sensory', 'processing', 'I become overwhelmed in busy or crowded environments', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.3, 5, 18, true),
('sensory', 'integration', 'I have difficulty filtering out background noise to focus on important sounds', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),

-- MOTOR SKILLS Questions (Category: motor)
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('motor', 'fine_motor', 'I have difficulty with handwriting and my writing is messy', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('motor', 'fine_motor', 'I struggle with tasks like tying shoes, buttoning clothes, or using utensils', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 15, true),
('motor', 'gross_motor', 'I have trouble with sports or physical activities that require coordination', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),
('motor', 'planning', 'I have difficulty learning new physical skills or movements', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('motor', 'bilateral', 'I have trouble using both hands together for tasks', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 5, 18, true),

-- EMOTIONAL REGULATION Questions (Category: emotional)
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max, active) VALUES
('emotional', 'regulation', 'I have intense emotional reactions that seem bigger than the situation', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.2, 5, 18, true),
('emotional', 'regulation', 'I have difficulty calming down when I''m upset', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true),
('emotional', 'anxiety', 'I worry excessively about school performance or social situations', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.0, 8, 18, true),
('emotional', 'self_esteem', 'I often feel different from other students my age', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 8, 18, true),
('emotional', 'frustration', 'I get frustrated easily when tasks are difficult', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"], "scores": [1, 2, 3, 4, 5]}', 1.1, 5, 18, true);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_assessment_questions_category ON assessment_questions(category);
CREATE INDEX IF NOT EXISTS idx_assessment_questions_age ON assessment_questions(age_min, age_max);
CREATE INDEX IF NOT EXISTS idx_assessment_questions_active ON assessment_questions(active);
CREATE INDEX IF NOT EXISTS idx_assessment_questions_category_age ON assessment_questions(category, age_min, age_max, active);

-- View total questions by category
SELECT 
    category,
    subcategory,
    COUNT(*) as question_count,
    AVG(scoring_weight) as avg_weight,
    MIN(age_min) as min_age,
    MAX(age_max) as max_age
FROM assessment_questions 
WHERE active = true
GROUP BY category, subcategory
ORDER BY category, subcategory;