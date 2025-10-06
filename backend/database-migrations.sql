-- ThinkAble Adaptive UI Database Schema Extensions
-- Run these migrations to add neurodivergent assessment capabilities

-- 1. Neurodivergent Assessment Results
CREATE TABLE user_assessments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attention_score INTEGER DEFAULT 0,
    social_communication_score INTEGER DEFAULT 0,
    sensory_processing_score INTEGER DEFAULT 0,
    reading_difficulty_score INTEGER DEFAULT 0,
    motor_skills_score INTEGER DEFAULT 0,
    font_preferences JSONB,
    ui_adaptations JSONB,
    recommended_preset VARCHAR(50) DEFAULT 'standard',
    assessment_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_user_assessments_user_id ON user_assessments(user_id);
CREATE INDEX idx_user_assessments_preset ON user_assessments(recommended_preset);

-- 2. Font Testing Results (for dyslexia detection)
CREATE TABLE font_test_results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    font_name VARCHAR(100) NOT NULL,
    readability_rating INTEGER CHECK (readability_rating >= 1 AND readability_rating <= 5),
    reading_time_ms INTEGER,
    difficulty_reported VARCHAR(20) CHECK (difficulty_reported IN ('easy', 'medium', 'hard')),
    symptoms_reported JSONB, -- letters_move, eye_strain, slow_reading, etc.
    test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_font_test_results_user_id ON font_test_results(user_id);
CREATE INDEX idx_font_test_results_font ON font_test_results(font_name);

-- 3. Enhanced User Preferences (adaptive UI settings)
CREATE TABLE adaptive_ui_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    ui_preset VARCHAR(50) DEFAULT 'standard',
    font_family VARCHAR(100) DEFAULT 'Arial, sans-serif',
    font_size INTEGER DEFAULT 16 CHECK (font_size >= 12 AND font_size <= 28),
    line_height DECIMAL(3,1) DEFAULT 1.5 CHECK (line_height >= 1.0 AND line_height <= 3.0),
    background_color VARCHAR(20) DEFAULT '#ffffff',
    text_color VARCHAR(20) DEFAULT '#333333',
    contrast_level VARCHAR(20) DEFAULT 'normal' CHECK (contrast_level IN ('normal', 'high', 'extra-high')),
    animations_enabled BOOLEAN DEFAULT TRUE,
    timer_style VARCHAR(20) DEFAULT 'standard',
    break_intervals INTEGER DEFAULT 25 CHECK (break_intervals >= 5 AND break_intervals <= 60),
    auto_applied BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_adaptive_ui_settings_user_id ON adaptive_ui_settings(user_id);
CREATE INDEX idx_adaptive_ui_settings_preset ON adaptive_ui_settings(ui_preset);

-- 4. Assessment Questions Bank
CREATE TABLE assessment_questions (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL CHECK (category IN ('attention', 'social', 'sensory', 'reading', 'motor')),
    subcategory VARCHAR(50),
    question_text TEXT NOT NULL,
    question_type VARCHAR(30) DEFAULT 'likert' CHECK (question_type IN ('likert', 'binary', 'font_test', 'multiple_choice')),
    options JSONB, -- For storing answer options and scoring weights
    scoring_weight DECIMAL(3,2) DEFAULT 1.0 CHECK (scoring_weight >= 0.1 AND scoring_weight <= 2.0),
    age_min INTEGER DEFAULT 5 CHECK (age_min >= 5),
    age_max INTEGER DEFAULT 18 CHECK (age_max <= 25 AND age_max >= age_min),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_assessment_questions_category ON assessment_questions(category);
CREATE INDEX idx_assessment_questions_age ON assessment_questions(age_min, age_max);
CREATE INDEX idx_assessment_questions_active ON assessment_questions(active);

-- 5. Interaction Analytics (enhance existing progress tracking)
CREATE TABLE ui_interaction_analytics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(100),
    interaction_type VARCHAR(50), -- 'click', 'scroll', 'focus', 'timer_start', etc.
    element_type VARCHAR(50), -- 'button', 'link', 'timer', 'lesson', etc.
    ui_preset VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success_indicator BOOLEAN DEFAULT TRUE,
    time_spent_seconds INTEGER,
    additional_data JSONB -- For storing extra interaction metadata
);

-- Index for performance
CREATE INDEX idx_ui_interaction_analytics_user_id ON ui_interaction_analytics(user_id);
CREATE INDEX idx_ui_interaction_analytics_session ON ui_interaction_analytics(session_id);
CREATE INDEX idx_ui_interaction_analytics_timestamp ON ui_interaction_analytics(timestamp);
CREATE INDEX idx_ui_interaction_analytics_preset ON ui_interaction_analytics(ui_preset);

-- 6. Assessment Response Storage
CREATE TABLE assessment_responses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    question_id BIGINT REFERENCES assessment_questions(id) ON DELETE CASCADE,
    response_value INTEGER, -- For likert/numeric responses
    response_text TEXT, -- For text responses
    response_time_ms INTEGER, -- Time taken to answer
    session_id VARCHAR(100), -- To group responses from same session
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_assessment_responses_user_id ON assessment_responses(user_id);
CREATE INDEX idx_assessment_responses_session ON assessment_responses(session_id);

-- 7. Trigger to update last_updated timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to user_assessments
CREATE TRIGGER update_user_assessments_updated_at 
    BEFORE UPDATE ON user_assessments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to adaptive_ui_settings
CREATE TRIGGER update_adaptive_ui_settings_updated_at 
    BEFORE UPDATE ON adaptive_ui_settings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 8. Insert default assessment questions
INSERT INTO assessment_questions (category, subcategory, question_text, question_type, options, scoring_weight, age_min, age_max) VALUES

-- Attention/ADHD Questions
('attention', 'focus', 'How often do you find it difficult to focus on tasks or activities for more than 15-20 minutes?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 5, 25),
('attention', 'hyperactivity', 'How often do you feel the need to move around or fidget when you''re supposed to sit still?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 5, 25),
('attention', 'impulsivity', 'How often do you interrupt others or have trouble waiting your turn?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 5, 25),
('attention', 'organization', 'How often do you have trouble organizing tasks or managing time?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 8, 25),
('attention', 'distraction', 'How easily are you distracted by sounds, movement, or other things around you?', 'likert', '{"scale": 5, "labels": ["Not easily", "Slightly", "Moderately", "Very easily", "Extremely easily"]}', 1.0, 5, 25),

-- Reading/Dyslexia Questions
('reading', 'comprehension', 'How often do you need to read something several times to understand it?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 7, 25),
('reading', 'fluency', 'How do you feel about your reading speed compared to others your age?', 'likert', '{"scale": 5, "labels": ["Much faster", "Faster", "About the same", "Slower", "Much slower"]}', 1.0, 8, 25),
('reading', 'visual', 'When reading, do letters or words ever appear to move, blur, or jump around on the page?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.5, 6, 25),
('reading', 'fatigue', 'How quickly do your eyes get tired when reading?', 'likert', '{"scale": 5, "labels": ["Never", "After hours", "After 30 minutes", "After 15 minutes", "Very quickly"]}', 1.0, 6, 25),
('reading', 'spelling', 'How difficult is spelling for you?', 'likert', '{"scale": 5, "labels": ["Very easy", "Easy", "Moderate", "Difficult", "Very difficult"]}', 1.0, 7, 25),

-- Social Communication/Autism Questions
('social', 'communication', 'How comfortable are you with making eye contact during conversations?', 'likert', '{"scale": 5, "labels": ["Very comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very uncomfortable"]}', 1.0, 6, 25),
('social', 'patterns', 'How important are routines and predictable schedules to you?', 'likert', '{"scale": 5, "labels": ["Not important", "Slightly important", "Moderately important", "Very important", "Extremely important"]}', 1.0, 5, 25),
('social', 'literal', 'Do you sometimes have trouble understanding when people are joking or being sarcastic?', 'likert', '{"scale": 5, "labels": ["Never", "Rarely", "Sometimes", "Often", "Always"]}', 1.0, 8, 25),
('social', 'interests', 'Do you have special interests or hobbies that you like to focus on intensely?', 'binary', '{"options": ["Yes", "No"]}', 1.0, 5, 25),
('social', 'change', 'How do you typically react to unexpected changes in plans or routines?', 'likert', '{"scale": 5, "labels": ["Handle easily", "Adapt well", "Some difficulty", "Significant stress", "Very distressing"]}', 1.0, 6, 25),

-- Sensory Processing Questions
('sensory', 'sound', 'How do you react to loud or unexpected sounds?', 'likert', '{"scale": 5, "labels": ["No reaction", "Mild notice", "Some discomfort", "Strong discomfort", "Cannot tolerate"]}', 1.0, 5, 25),
('sensory', 'light', 'How sensitive are you to bright lights or flickering lights?', 'likert', '{"scale": 5, "labels": ["Not sensitive", "Slightly sensitive", "Moderately sensitive", "Very sensitive", "Extremely sensitive"]}', 1.0, 5, 25),
('sensory', 'texture', 'How do you feel about different textures (clothing, food, materials)?', 'likert', '{"scale": 5, "labels": ["Love variety", "Enjoy most", "Some preferences", "Strong preferences", "Very limited tolerance"]}', 1.0, 5, 25),
('sensory', 'crowds', 'How comfortable are you in busy, crowded environments?', 'likert', '{"scale": 5, "labels": ["Very comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very uncomfortable"]}', 1.0, 6, 25),

-- Motor Skills Questions
('motor', 'fine', 'How comfortable are you with tasks requiring fine motor skills (writing, drawing, using small objects)?', 'likert', '{"scale": 5, "labels": ["Very comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very uncomfortable"]}', 1.0, 5, 25),
('motor', 'coordination', 'How would you rate your coordination and balance?', 'likert', '{"scale": 5, "labels": ["Excellent", "Good", "Average", "Below average", "Poor"]}', 1.0, 6, 25);

-- 9. Create a view for easy assessment analysis
CREATE VIEW assessment_analysis AS
SELECT 
    u.id as user_id,
    u.email,
    u.name,
    ua.assessment_date,
    ua.attention_score,
    ua.social_communication_score,
    ua.sensory_processing_score,
    ua.reading_difficulty_score,
    ua.motor_skills_score,
    ua.recommended_preset,
    ua.assessment_completed,
    aus.ui_preset as current_ui_preset,
    aus.font_family,
    aus.font_size
FROM users u
LEFT JOIN user_assessments ua ON u.id = ua.user_id
LEFT JOIN adaptive_ui_settings aus ON u.id = aus.user_id;

-- 10. Grant necessary permissions (adjust role name as needed)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_app_user;

-- Verification queries
-- SELECT 'Assessment tables created successfully' as status;
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '%assessment%' OR table_name LIKE '%adaptive%';