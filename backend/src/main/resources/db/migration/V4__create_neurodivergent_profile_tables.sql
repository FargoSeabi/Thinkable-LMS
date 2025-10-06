-- Individual Neurodivergent Profile Database Schema
-- Supports comprehensive, personalized learning companion data

-- Main individual profile table
CREATE TABLE user_neurodivergent_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_name VARCHAR(100),
    pronouns VARCHAR(50),
    
    -- Multi-dimensional traits (0-10 scales)
    hyperfocus_intensity INT DEFAULT 5,
    attention_flexibility INT DEFAULT 5,
    sensory_processing INT DEFAULT 5,
    executive_function INT DEFAULT 5,
    social_battery INT DEFAULT 5,
    change_adaptability INT DEFAULT 5,
    emotional_regulation INT DEFAULT 5,
    information_processing INT DEFAULT 5,
    creativity_expression INT DEFAULT 5,
    structure_preference INT DEFAULT 5,
    
    -- Focus characteristics
    optimal_session_length INT DEFAULT 25,
    natural_rhythm VARCHAR(20) DEFAULT 'morning',
    hyperfocus_warning_time INT DEFAULT 90,
    transition_time INT DEFAULT 5,
    deep_work_capacity DECIMAL(3,1) DEFAULT 2.0,
    
    -- Sensory preferences
    auditory_preference VARCHAR(20) DEFAULT 'moderate',
    visual_preference VARCHAR(20) DEFAULT 'calm',
    tactile_comfort VARCHAR(20) DEFAULT 'smooth',
    movement_need VARCHAR(20) DEFAULT 'moderate',
    light_sensitivity VARCHAR(20) DEFAULT 'medium',
    temperature_preference VARCHAR(20) DEFAULT 'cool',
    
    -- Energy and emotional patterns
    daily_pattern VARCHAR(20) DEFAULT 'variable',
    
    -- Learning preferences
    primary_learning_style VARCHAR(20) DEFAULT 'mixed',
    information_chunking VARCHAR(20) DEFAULT 'small',
    feedback_preference VARCHAR(20) DEFAULT 'gentle',
    mistake_handling VARCHAR(20) DEFAULT 'supportive',
    motivation_style VARCHAR(20) DEFAULT 'internal',
    challenge_level VARCHAR(20) DEFAULT 'adaptive',
    learning_environment VARCHAR(20) DEFAULT 'quiet',
    time_preference VARCHAR(20) DEFAULT 'flexible',
    
    -- Support preferences
    celebration_style VARCHAR(20) DEFAULT 'quiet',
    communication_style VARCHAR(20) DEFAULT 'direct',
    autonomy_level VARCHAR(20) DEFAULT 'high',
    privacy_needs VARCHAR(20) DEFAULT 'respected',
    
    -- Goal and growth tracking
    adaptation_speed VARCHAR(20) DEFAULT 'gradual',
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version VARCHAR(10) DEFAULT '1.0',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_profile (user_id)
);

-- Personal triggers and preferences (many-to-many relationships)
CREATE TABLE user_distraction_triggers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    trigger_name VARCHAR(100) NOT NULL,
    severity INT DEFAULT 5,
    context VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_focus_enhancers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    enhancer_name VARCHAR(100) NOT NULL,
    effectiveness INT DEFAULT 5,
    context VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_break_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    break_type VARCHAR(50) NOT NULL,
    preference_level INT DEFAULT 5,
    duration_minutes INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_overwhelm_signs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sign_description VARCHAR(200) NOT NULL,
    early_warning BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_calming_strategies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    strategy_name VARCHAR(100) NOT NULL,
    effectiveness INT DEFAULT 5,
    context VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_personal_mantras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mantra_text TEXT NOT NULL,
    category VARCHAR(50),
    effectiveness_rating INT DEFAULT 5,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_coping_mechanisms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mechanism_name VARCHAR(100) NOT NULL,
    mechanism_type VARCHAR(50),
    effectiveness INT DEFAULT 5,
    context VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tool usage and learning data
CREATE TABLE user_tool_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tool_name VARCHAR(50) NOT NULL,
    tool_context VARCHAR(100),
    usage_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_duration_minutes INT,
    success_rating INT,
    user_energy_level INT,
    time_of_day VARCHAR(20),
    day_of_week INT,
    activity_context VARCHAR(50),
    additional_data JSON,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_tool_usage (user_id, tool_name, usage_timestamp),
    INDEX idx_usage_timestamp (usage_timestamp)
);

-- Pattern recognition and insights
CREATE TABLE user_learning_patterns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pattern_type VARCHAR(50) NOT NULL,
    pattern_data JSON NOT NULL,
    confidence_score DECIMAL(3,2) DEFAULT 0.50,
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validated_by_user BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_patterns (user_id, pattern_type, active)
);

CREATE TABLE user_adaptive_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    insight_type VARCHAR(50) NOT NULL,
    insight_title VARCHAR(200) NOT NULL,
    insight_description TEXT NOT NULL,
    insight_data JSON,
    confidence_score DECIMAL(3,2) DEFAULT 0.50,
    priority_level VARCHAR(20) DEFAULT 'medium',
    presented_to_user BOOLEAN DEFAULT FALSE,
    user_response VARCHAR(20), -- 'accepted', 'rejected', 'pending'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_insights (user_id, insight_type, user_response)
);

-- Energy and focus tracking
CREATE TABLE user_energy_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    energy_level INT NOT NULL, -- 1-10 scale
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    context VARCHAR(100),
    notes TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_energy_logs (user_id, logged_at)
);

CREATE TABLE user_focus_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    task_description VARCHAR(500),
    planned_duration_minutes INT NOT NULL,
    actual_duration_minutes INT,
    completed BOOLEAN DEFAULT FALSE,
    effectiveness_rating INT, -- 1-10 scale
    breaks_taken INT DEFAULT 0,
    interruptions INT DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    session_data JSON,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_focus_sessions (user_id, started_at)
);

-- Growth and achievement tracking
CREATE TABLE user_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_type VARCHAR(50) NOT NULL,
    achievement_description VARCHAR(200) NOT NULL,
    points_earned INT DEFAULT 0,
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    milestone_data JSON,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_achievements (user_id, achievement_type, achieved_at)
);

-- Personal goals and objectives
CREATE TABLE user_learning_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    goal_description TEXT NOT NULL,
    target_date DATE,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'active', -- 'active', 'completed', 'paused', 'cancelled'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    goal_data JSON,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_goals (user_id, status, target_date)
);

-- Adaptive recommendations and suggestions
CREATE TABLE user_adaptive_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    recommendation_data JSON,
    priority_level VARCHAR(20) DEFAULT 'medium',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    presented_at TIMESTAMP NULL,
    user_response VARCHAR(20), -- 'accepted', 'rejected', 'pending', 'ignored'
    responded_at TIMESTAMP NULL,
    effectiveness_rating INT, -- User feedback on how helpful it was
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_recommendations (user_id, recommendation_type, user_response)
);

-- Assessment integration
CREATE TABLE user_assessment_neurodivergent_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assessment_id BIGINT,
    raw_responses JSON NOT NULL,
    processed_profile JSON NOT NULL,
    confidence_scores JSON,
    recommendations JSON,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_assessment_results (user_id, completed_at)
);