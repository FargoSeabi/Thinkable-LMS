/**
 * Professional Dynamic Font Loader
 * Loads OpenDyslexic fonts from the correct backend URL for any environment
 * Single source of truth - no CSS conflicts or race conditions
 */

interface FontDefinition {
  family: string;
  src: string;
  weight: string;
  style: string;
}

export const loadOpenDyslexicFonts = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    try {
      const backendUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
      
      const fonts: FontDefinition[] = [
        {
          family: 'OpenDyslexic',
          src: `${backendUrl}/fonts/OpenDyslexicAlta-Regular.otf`,
          weight: 'normal',
          style: 'normal'
        },
        {
          family: 'OpenDyslexic',
          src: `${backendUrl}/fonts/OpenDyslexicAlta-Bold.otf`,
          weight: 'bold',
          style: 'normal'
        },
        {
          family: 'OpenDyslexic',
          src: `${backendUrl}/fonts/OpenDyslexicAlta-Italic.otf`,
          weight: 'normal',
          style: 'italic'
        },
        {
          family: 'OpenDyslexic',
          src: `${backendUrl}/fonts/OpenDyslexicAlta-BoldItalic.otf`,
          weight: 'bold',
          style: 'italic'
        },
        {
          family: 'OpenDyslexicMono',
          src: `${backendUrl}/fonts/OpenDyslexicMono-Regular.otf`,
          weight: 'normal',
          style: 'normal'
        }
      ];

      const fontCSS = fonts.map(font => `
        @font-face {
          font-family: '${font.family}';
          src: url('${font.src}') format('opentype');
          font-weight: ${font.weight};
          font-style: ${font.style};
          font-display: swap;
        }
      `).join('\n');

      // Remove any existing font definitions
      const existingStyle = document.getElementById('opendyslexic-fonts');
      if (existingStyle) {
        existingStyle.remove();
      }

      // Inject new font definitions
      const style = document.createElement('style');
      style.id = 'opendyslexic-fonts';
      style.textContent = fontCSS;
      document.head.appendChild(style);

      // Use FontFace API to preload fonts for better performance
      if ('fonts' in document) {
        const fontPromises = fonts.map(font => {
          const fontFace = new FontFace(font.family, `url(${font.src})`, {
            weight: font.weight,
            style: font.style,
            display: 'swap'
          });
          
          return fontFace.load().then(loadedFace => {
            (document as any).fonts.add(loadedFace);
            return loadedFace;
          });
        });

        Promise.all(fontPromises)
          .then(() => {
            console.log('✅ OpenDyslexic fonts loaded successfully');
            resolve();
          })
          .catch((error) => {
            console.warn('⚠️ Some OpenDyslexic fonts failed to load:', error);
            resolve(); // Don't reject - fonts are still defined in CSS
          });
      } else {
        // Fallback for older browsers
        console.log('✅ OpenDyslexic fonts defined (legacy browser)');
        resolve();
      }

    } catch (error) {
      console.error('❌ Failed to load OpenDyslexic fonts:', error);
      reject(error);
    }
  });
};

// Simplified export for backward compatibility
export const injectDynamicFontCSS = loadOpenDyslexicFonts;