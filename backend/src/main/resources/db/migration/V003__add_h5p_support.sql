-- Migration to add H5P interactive content support to learning_content table
-- Compatible with existing PostgreSQL schema and Spring Boot 3.2.5

-- Add new enum column for content types (nullable initially for compatibility)
ALTER TABLE learning_content 
ADD COLUMN content_type_enum VARCHAR(20);

-- Add H5P-specific columns
ALTER TABLE learning_content 
ADD COLUMN h5p_content_id VARCHAR(100),
ADD COLUMN h5p_library VARCHAR(100),
ADD COLUMN h5p_metadata TEXT,
ADD COLUMN h5p_embed_url VARCHAR(1000),
ADD COLUMN h5p_settings TEXT;

-- Create indexes for performance
CREATE INDEX idx_learning_content_type_enum ON learning_content(content_type_enum);
CREATE INDEX idx_learning_content_h5p_id ON learning_content(h5p_content_id);

-- Populate the new enum column based on existing content_type values
UPDATE learning_content 
SET content_type_enum = CASE 
    WHEN content_type = 'document' THEN 'DOCUMENT'
    WHEN content_type = 'video' THEN 'VIDEO'
    WHEN content_type = 'audio' THEN 'AUDIO'
    WHEN content_type = 'image' THEN 'IMAGE'
    WHEN content_type = 'interactive' THEN 'INTERACTIVE'
    ELSE 'DOCUMENT'
END;

-- Add comments for documentation
COMMENT ON COLUMN learning_content.content_type_enum IS 'Enum-based content type (DOCUMENT, VIDEO, AUDIO, IMAGE, INTERACTIVE)';
COMMENT ON COLUMN learning_content.h5p_content_id IS 'Unique identifier for H5P content';
COMMENT ON COLUMN learning_content.h5p_library IS 'H5P content library type (e.g., H5P.InteractiveVideo)';
COMMENT ON COLUMN learning_content.h5p_metadata IS 'JSON metadata for H5P content configuration';
COMMENT ON COLUMN learning_content.h5p_embed_url IS 'Direct URL to embed H5P content';
COMMENT ON COLUMN learning_content.h5p_settings IS 'JSON settings for H5P player configuration';

-- Add constraint to ensure H5P fields are only used for interactive content
ALTER TABLE learning_content 
ADD CONSTRAINT chk_h5p_fields_for_interactive 
CHECK (
    (content_type_enum = 'INTERACTIVE' AND h5p_content_id IS NOT NULL) OR
    (content_type_enum != 'INTERACTIVE' AND h5p_content_id IS NULL)
);

-- Optional: Add constraint for valid content types
ALTER TABLE learning_content 
ADD CONSTRAINT chk_valid_content_type_enum 
CHECK (content_type_enum IN ('DOCUMENT', 'VIDEO', 'AUDIO', 'IMAGE', 'INTERACTIVE'));