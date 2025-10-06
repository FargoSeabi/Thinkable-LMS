import React from 'react';

interface IconProps {
  name: string;
  size?: number | string;
  className?: string;
  color?: string;
  style?: React.CSSProperties;
}

const Icon: React.FC<IconProps> = ({
  name,
  size = 24,
  className = '',
  color,
  style = {}
}) => {
  const iconStyle: React.CSSProperties = {
    width: size,
    height: size,
    display: 'inline-block',
    fill: color || 'currentColor',
    ...style
  };

  const getIconPath = (iconName: string) => {
    const iconMap: { [key: string]: string } = {
      'trophy': 'trophy-cup-svgrepo-com.svg',
      'star': 'star-shine-svgrepo-com.svg',
      'books': 'books-and-people-svgrepo-com.svg',
      'fire': 'fire-svgrepo-com.svg',
      'bullseye': 'bullseye-arrow-svgrepo-com.svg',
      'accessibility': 'accessibility-svgrepo-com.svg',
      'celebration': 'celebration-party-winter-svgrepo-com.svg',
      'awesome': 'awesome-o-svgrepo-com.svg',
      'search': 'search-alt-2-svgrepo-com.svg',
      'timer': 'timer-svgrepo-com.svg',
      'egghead': 'egghead-svgrepo-com.svg',
      'cookie': 'cookie-svgrepo-com.svg'
    };

    return iconMap[iconName] || `${iconName}.svg`;
  };

  try {
    const iconFileName = getIconPath(name);
    const iconSrc = require(`../../assets/icons/${iconFileName}`);

    return (
      <img
        src={iconSrc}
        alt={`${name} icon`}
        style={iconStyle}
        className={`icon icon-${name} ${className}`}
      />
    );
  } catch (error) {
    // Fallback to a simple div if icon is not found
    console.warn(`Icon "${name}" not found`);
    return (
      <div
        style={{ ...iconStyle, backgroundColor: '#ccc', borderRadius: '2px' }}
        className={`icon icon-fallback ${className}`}
        title={`${name} icon`}
      />
    );
  }
};

export default Icon;