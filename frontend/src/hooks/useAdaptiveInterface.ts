import { useEffect, useCallback } from 'react';
import { useAdaptiveUI } from '../contexts/AdaptiveUIContext';

/**
 * Advanced hook that automatically applies comprehensive UI adaptations
 * based on user's neurodivergent profile and assessment results
 */
export const useAdaptiveInterface = () => {
  const { currentPreset } = useAdaptiveUI();

  const adaptExistingContent = useCallback(() => {
    // Adapt all buttons to use consistent styling
    document.querySelectorAll('button:not(.adaptive-processed)').forEach(button => {
      button.classList.add('adaptive-button', 'adaptive-processed');
    });

    // Adapt all form inputs
    document.querySelectorAll('input:not(.adaptive-processed), textarea:not(.adaptive-processed), select:not(.adaptive-processed)').forEach(input => {
      input.classList.add('adaptive-input', 'adaptive-processed');
    });

    // Adapt all cards and containers
    document.querySelectorAll('.card:not(.adaptive-processed), .container:not(.adaptive-processed)').forEach(card => {
      card.classList.add('adaptive-card', 'adaptive-processed');
    });

    // Adapt all headings
    document.querySelectorAll('h1:not(.adaptive-processed), h2:not(.adaptive-processed), h3:not(.adaptive-processed), h4:not(.adaptive-processed), h5:not(.adaptive-processed), h6:not(.adaptive-processed)').forEach(heading => {
      heading.classList.add('adaptive-heading', 'adaptive-processed');
    });

    // Adapt all paragraphs and text blocks
    document.querySelectorAll('p:not(.adaptive-processed), .text:not(.adaptive-processed)').forEach(text => {
      text.classList.add('adaptive-text', 'adaptive-processed');
    });

    // Add reading aids for dyslexia
    if (currentPreset === 'READING_SUPPORT') {
      addReadingAids();
    }

    // Add focus management for ADHD
    if (currentPreset === 'FOCUS_ENHANCED' || currentPreset === 'FOCUS_CALM') {
      addFocusManagement();
    }
  }, [currentPreset]);

  const applyGlobalAdaptations = useCallback(() => {
    const body = document.body;
    const root = document.documentElement;

    // Remove all existing adaptation classes
    body.classList.remove(
      'dyslexia-adaptive', 'adhd-adaptive', 'autism-adaptive', 
      'sensory-adaptive', 'focus-adaptive', 'reading-adaptive'
    );

    // Apply preset-specific global adaptations
    switch (currentPreset) {
      case 'READING_SUPPORT':
        applyDyslexiaAdaptations(body, root);
        break;
      case 'FOCUS_ENHANCED':
        applyADHDAdaptations(body, root);
        break;
      case 'FOCUS_CALM':
        applyFocusCalmAdaptations(body, root);
        break;
      case 'SOCIAL_SIMPLE':
        applyAutismAdaptations(body, root);
        break;
      case 'SENSORY_CALM':
        applySensoryAdaptations(body, root);
        break;
      default:
        applyStandardAdaptations(body, root);
    }

    // Apply cross-cutting accessibility enhancements
    applyAccessibilityEnhancements(body, root);
    
    // Apply dynamic content adaptations
    adaptExistingContent();

  }, [currentPreset, adaptExistingContent]);

  const applyDyslexiaAdaptations = (body: HTMLElement, root: HTMLElement) => {
    body.classList.add('dyslexia-adaptive');
    
    // Enhanced text readability
    root.style.setProperty('--adaptive-word-spacing', '3px');
    root.style.setProperty('--adaptive-paragraph-spacing', '2rem');
    root.style.setProperty('--adaptive-heading-spacing', '1.5rem');
    
    // Reduce text density
    root.style.setProperty('--adaptive-text-max-width', '70ch');
    root.style.setProperty('--adaptive-line-length', '60ch');
    
    // Enhanced focus indicators for reading
    root.style.setProperty('--adaptive-reading-highlight', '#fff3cd');
    root.style.setProperty('--adaptive-selection-color', '#ffd700');
    
    // Disable problematic animations that can cause text movement
    root.style.setProperty('--adaptive-disable-text-animations', 'true');
  };

  const applyADHDAdaptations = (body: HTMLElement, root: HTMLElement) => {
    body.classList.add('adhd-adaptive');
    
    // Reduced visual clutter
    root.style.setProperty('--adaptive-shadow-intensity', '0.05');
    root.style.setProperty('--adaptive-border-prominence', '2px');
    root.style.setProperty('--adaptive-background-noise', 'none');
    
    // Enhanced focus management
    root.style.setProperty('--adaptive-focus-ring-size', '4px');
    root.style.setProperty('--adaptive-focus-animation', 'pulse 1.5s infinite');
    root.style.setProperty('--adaptive-distraction-filter', 'blur(0.5px)');
    
    // Simplified navigation
    root.style.setProperty('--adaptive-nav-complexity', 'minimal');
    root.style.setProperty('--adaptive-button-grouping', 'separated');
  };

  const applyFocusCalmAdaptations = (body: HTMLElement, root: HTMLElement) => {
    body.classList.add('focus-adaptive');
    
    // Calming color palette
    root.style.setProperty('--adaptive-stress-colors', 'muted');
    root.style.setProperty('--adaptive-contrast-level', 'medium');
    
    // Gentle animations
    root.style.setProperty('--adaptive-motion-preference', 'reduced');
    root.style.setProperty('--adaptive-transition-easing', 'ease-out');
  };

  const applyAutismAdaptations = (body: HTMLElement, root: HTMLElement) => {
    body.classList.add('autism-adaptive');
    
    // Predictable layouts
    root.style.setProperty('--adaptive-layout-consistency', 'strict');
    root.style.setProperty('--adaptive-navigation-stability', 'fixed');
    
    // Clear visual hierarchy
    root.style.setProperty('--adaptive-heading-distinctiveness', 'high');
    root.style.setProperty('--adaptive-section-separation', '3rem');
    
    // Explicit interaction cues
    root.style.setProperty('--adaptive-hover-feedback', 'explicit');
    root.style.setProperty('--adaptive-click-affordance', 'clear');
  };

  const applySensoryAdaptations = (body: HTMLElement, root: HTMLElement) => {
    body.classList.add('sensory-adaptive');
    
    // Reduced sensory overload
    root.style.setProperty('--adaptive-color-saturation', '0.7');
    root.style.setProperty('--adaptive-animation-intensity', '0.3');
    root.style.setProperty('--adaptive-shadow-softness', 'soft');
    
    // Gentle transitions
    root.style.setProperty('--adaptive-state-changes', 'gradual');
    root.style.setProperty('--adaptive-loading-style', 'subtle');
  };

  const applyStandardAdaptations = (body: HTMLElement, root: HTMLElement) => {
    // Reset to standard but still accessible defaults
    root.style.setProperty('--adaptive-enhancement-level', 'standard');
  };

  const applyAccessibilityEnhancements = (body: HTMLElement, root: HTMLElement) => {
    // Universal accessibility improvements
    root.style.setProperty('--adaptive-skip-links', 'visible');
    root.style.setProperty('--adaptive-focus-management', 'enhanced');
    root.style.setProperty('--adaptive-error-visibility', 'prominent');
    root.style.setProperty('--adaptive-success-feedback', 'clear');
    
    // Responsive text scaling
    const baseSize = parseInt(getComputedStyle(root).getPropertyValue('--font-size-base') || '16');
    root.style.setProperty('--adaptive-text-scale-factor', `${Math.max(1, baseSize / 16)}`);
  };


  const addReadingAids = () => {
    // Add reading guide lines
    const style = document.createElement('style');
    style.textContent = `
      .dyslexia-adaptive p:hover {
        background: linear-gradient(90deg, transparent 0%, var(--adaptive-reading-highlight, #fff3cd) 2%, var(--adaptive-reading-highlight, #fff3cd) 98%, transparent 100%);
        padding: 0.25rem 0.5rem;
        border-radius: var(--border-radius, 4px);
      }
      
      .dyslexia-adaptive ::selection {
        background: var(--adaptive-selection-color, #ffd700);
        color: #000;
      }
      
      .dyslexia-adaptive .adaptive-text {
        max-width: var(--adaptive-text-max-width, 70ch);
      }
    `;
    
    if (!document.head.querySelector('#dyslexia-reading-aids')) {
      style.id = 'dyslexia-reading-aids';
      document.head.appendChild(style);
    }
  };

  const addFocusManagement = () => {
    // Enhanced keyboard navigation
    const style = document.createElement('style');
    style.textContent = `
      .adhd-adaptive *:focus,
      .focus-adaptive *:focus {
        outline: var(--adaptive-focus-ring-size, 3px) solid var(--focus-color);
        outline-offset: 2px;
        animation: var(--adaptive-focus-animation, none);
      }
      
      .adhd-adaptive .adaptive-button:not(:focus):not(:hover) {
        filter: var(--adaptive-distraction-filter, none);
      }
      
      @keyframes pulse {
        0%, 100% { outline-color: var(--focus-color); }
        50% { outline-color: transparent; }
      }
    `;
    
    if (!document.head.querySelector('#focus-management-aids')) {
      style.id = 'focus-management-aids';
      document.head.appendChild(style);
    }
  };

  // Apply adaptations when preset changes
  useEffect(() => {
    // Small delay to ensure DOM is ready
    const timer = setTimeout(applyGlobalAdaptations, 100);
    return () => clearTimeout(timer);
  }, [applyGlobalAdaptations]);

  // Re-apply adaptations when new content is added to the DOM
  useEffect(() => {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
          // Adapt new content after a brief delay
          setTimeout(adaptExistingContent, 50);
        }
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });

    return () => observer.disconnect();
  }, [currentPreset, adaptExistingContent]);

  return {
    currentPreset,
    applyGlobalAdaptations,
    isAdaptive: currentPreset !== 'STANDARD_ADAPTIVE'
  };
};