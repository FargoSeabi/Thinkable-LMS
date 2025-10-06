package com.thinkable.backend.enums;

/**
 * Enumeration for different types of learning content
 * Supports traditional file-based content and interactive H5P content
 */
public enum ContentType {
    DOCUMENT("document", "Document-based content (PDF, DOCX, etc.)"),
    VIDEO("video", "Video content (MP4, AVI, etc.)"),
    AUDIO("audio", "Audio content (MP3, WAV, etc.)"),
    IMAGE("image", "Image content (JPG, PNG, etc.)"),
    INTERACTIVE("interactive", "Interactive H5P content");

    private final String value;
    private final String description;

    ContentType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ContentType fromString(String value) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return DOCUMENT; // Default fallback
    }

    public boolean isInteractive() {
        return this == INTERACTIVE;
    }

    public boolean isFileBasedContent() {
        return this != INTERACTIVE;
    }

    @Override
    public String toString() {
        return value;
    }
}
