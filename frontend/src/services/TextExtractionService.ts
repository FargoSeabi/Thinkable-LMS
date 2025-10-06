// Text Extraction Service for Accessibility
// Extracts text from various content types and applies accessibility presets

import config from './config';

export interface ExtractedContent {
  title: string;
  text: string;
  metadata: {
    contentType: string;
    fileName: string;
    extractionMethod: string;
    confidence: number;
    wordCount: number;
    readingTime: number; // in minutes
  };
  accessibility: {
    hasImages: boolean;
    hasStructure: boolean;
    hasFormats: boolean;
    imageDescriptions: string[];
  };
}

export interface TextExtractionOptions {
  includeImages: boolean;
  includeMetadata: boolean;
  applyPreset: string | null;
  maxLength: number;
  preserveStructure: boolean;
}

class TextExtractionService {
  private baseUrl = `${config.apiBaseUrl}/api/content`;

  /**
   * Extract text content from various file types
   */
  public async extractText(
    contentId: string, 
    contentType: string,
    options: Partial<TextExtractionOptions> = {}
  ): Promise<ExtractedContent> {
    const defaultOptions: TextExtractionOptions = {
      includeImages: true,
      includeMetadata: true,
      applyPreset: null,
      maxLength: 50000,
      preserveStructure: true
    };

    const extractionOptions = { ...defaultOptions, ...options };

    try {
      const response = await fetch(`${this.baseUrl}/${contentId}/extract-text`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify(extractionOptions)
      });

      if (!response.ok) {
        throw new Error(`Text extraction failed: ${response.status} ${response.statusText}`);
      }

      const extractedContent: ExtractedContent = await response.json();
      
      // Apply reading time calculation
      extractedContent.metadata.readingTime = this.calculateReadingTime(extractedContent.text);
      
      return extractedContent;
    } catch (error) {
      console.error('Text extraction error:', error);
      // Fallback to client-side extraction if possible
      return this.clientSideExtraction(contentType, extractionOptions);
    }
  }

  /**
   * Client-side text extraction fallback
   */
  private clientSideExtraction(
    contentType: string, 
    options: TextExtractionOptions
  ): ExtractedContent {
    // Basic fallback extraction
    const fallbackContent: ExtractedContent = {
      title: 'Content',
      text: `This ${contentType} file contains content that requires text extraction. Please ensure the backend extraction service is running for full text access.`,
      metadata: {
        contentType,
        fileName: 'unknown',
        extractionMethod: 'fallback',
        confidence: 0.1,
        wordCount: 0,
        readingTime: 0
      },
      accessibility: {
        hasImages: false,
        hasStructure: false,
        hasFormats: false,
        imageDescriptions: []
      }
    };

    fallbackContent.metadata.wordCount = this.countWords(fallbackContent.text);
    fallbackContent.metadata.readingTime = this.calculateReadingTime(fallbackContent.text);

    return fallbackContent;
  }

  /**
   * Extract text from images using OCR
   */
  public async extractFromImage(imageUrl: string): Promise<ExtractedContent> {
    try {
      const response = await fetch(`${config.apiBaseUrl}/api/content/ocr/extract`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify({ imageUrl })
      });

      if (!response.ok) {
        throw new Error(`OCR extraction failed: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('OCR extraction error:', error);
      return this.clientSideExtraction('image', {
        includeImages: true,
        includeMetadata: true,
        applyPreset: null,
        maxLength: 50000,
        preserveStructure: true
      });
    }
  }

  /**
   * Extract transcript from video/audio content
   */
  public async extractTranscript(contentId: string, mediaType: 'video' | 'audio'): Promise<ExtractedContent> {
    try {
      const response = await fetch(`${this.baseUrl}/${contentId}/transcript`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
      });

      if (!response.ok) {
        throw new Error(`Transcript extraction failed: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Transcript extraction error:', error);
      return this.clientSideExtraction(mediaType, {
        includeImages: false,
        includeMetadata: true,
        applyPreset: null,
        maxLength: 50000,
        preserveStructure: true
      });
    }
  }

  /**
   * Process and format extracted text for accessibility
   */
  public processForAccessibility(
    content: ExtractedContent, 
    preset: string
  ): ExtractedContent {
    let processedText = content.text;

    switch (preset) {
      case 'DYSLEXIA_SUPPORT':
        processedText = this.optimizeForDyslexia(processedText);
        break;
      case 'ADHD_SUPPORT':
        processedText = this.optimizeForADHD(processedText);
        break;
      case 'AUTISM_SUPPORT':
        processedText = this.optimizeForAutism(processedText);
        break;
      case 'SENSORY_CALM':
        processedText = this.optimizeForSensory(processedText);
        break;
      case 'READING_SUPPORT':
        processedText = this.optimizeForReading(processedText);
        break;
    }

    return {
      ...content,
      text: processedText,
      metadata: {
        ...content.metadata,
        wordCount: this.countWords(processedText),
        readingTime: this.calculateReadingTime(processedText)
      }
    };
  }

  /**
   * Optimization functions for different accessibility needs
   */
  private optimizeForDyslexia(text: string): string {
    return text
      // Add extra spacing between sentences
      .replace(/\. /g, '.  ')
      // Break up long paragraphs
      .replace(/(.{200,}?[.!?])\s/g, '$1\n\n')
      // Emphasize important words (can be styled with CSS)
      .replace(/\b(important|note|warning|remember|key|main)\b/gi, '**$1**');
  }

  private optimizeForADHD(text: string): string {
    return text
      // Create clear section breaks
      .replace(/\n\n/g, '\n\n---\n\n')
      // Add bullet points for lists
      .replace(/^(\d+[\.\)])\s/gm, '• ')
      // Highlight action words
      .replace(/\b(do|action|step|next|then|first|finally)\b/gi, '**$1**');
  }

  private optimizeForAutism(text: string): string {
    return text
      // Maintain consistent structure
      .replace(/\n{3,}/g, '\n\n')
      // Add clear headings
      .replace(/^([A-Z][^.!?]*):$/gm, '## $1')
      // Structure information predictably
      .replace(/^(.{0,50}[.!?])\s*$/gm, '**$1**\n');
  }

  private optimizeForSensory(text: string): string {
    return text
      // Remove overwhelming punctuation clusters
      .replace(/[!]{2,}/g, '!')
      .replace(/[?]{2,}/g, '?')
      // Add gentle breaks
      .replace(/\. /g, '.\n\n')
      // Soften capitalized text
      .replace(/[A-Z]{3,}/g, (match) => 
        match.charAt(0) + match.slice(1).toLowerCase()
      );
  }

  private optimizeForReading(text: string): string {
    return text
      // Enhanced readability spacing
      .replace(/([.!?])\s/g, '$1  ')
      // Clear paragraph breaks
      .replace(/\n/g, '\n\n')
      // Structured information
      .replace(/^(\w+):\s/gm, '**$1:** ');
  }

  /**
   * Create structured text with headers and sections
   */
  public createStructuredText(content: ExtractedContent, originalContent: any): string {
    const sections: string[] = [];

    // Title section
    if (content.title) {
      sections.push(`# ${content.title}\n`);
    }

    // Metadata section
    if (originalContent.subjectArea || originalContent.difficultyLevel) {
      sections.push('## Information');
      if (originalContent.subjectArea) sections.push(`**Subject:** ${originalContent.subjectArea}`);
      if (originalContent.difficultyLevel) sections.push(`**Difficulty:** ${originalContent.difficultyLevel}`);
      if (content.metadata.readingTime) sections.push(`**Reading Time:** ${content.metadata.readingTime} minutes`);
      sections.push('');
    }

    // Description section
    if (originalContent.description) {
      sections.push('## Description');
      sections.push(originalContent.description);
      sections.push('');
    }

    // Main content section
    if (content.text) {
      sections.push('## Content');
      sections.push(content.text);
    }

    // Accessibility information
    if (content.accessibility.imageDescriptions.length > 0) {
      sections.push('\n## Image Descriptions');
      content.accessibility.imageDescriptions.forEach((desc, index) => {
        sections.push(`**Image ${index + 1}:** ${desc}`);
      });
    }

    return sections.join('\n');
  }

  /**
   * Utility functions
   */
  private countWords(text: string): number {
    return text.trim().split(/\s+/).length;
  }

  private calculateReadingTime(text: string): number {
    const wordsPerMinute = 200; // Average reading speed
    const words = this.countWords(text);
    return Math.ceil(words / wordsPerMinute);
  }

  /**
   * Get content summary for quick overview
   */
  public createSummary(content: ExtractedContent, maxLength: number = 300): string {
    if (content.text.length <= maxLength) {
      return content.text;
    }

    // Extract first few sentences
    const sentences = content.text.split(/[.!?]+/).filter(s => s.trim().length > 0);
    let summary = '';
    
    for (const sentence of sentences) {
      if ((summary + sentence).length > maxLength) {
        break;
      }
      summary += sentence.trim() + '. ';
    }

    return summary + (content.text.length > summary.length ? '...' : '');
  }

  /**
   * Record text extraction usage for analytics
   */
  public recordUsage(action: string, contentType: string, extractionTime: number): void {
    try {
      fetch(`${config.apiBaseUrl}/api/accessibility/text-extraction-usage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action,
          contentType,
          extractionTime,
          timestamp: new Date().toISOString()
        })
      }).catch(error => console.warn('Failed to record text extraction usage:', error));
    } catch (error) {
      console.warn('Failed to record text extraction usage:', error);
    }
  }
}

// Export singleton instance
export const textExtractionService = new TextExtractionService();
export default TextExtractionService;