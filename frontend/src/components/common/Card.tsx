import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hoverEffect?: boolean;
  onClick?: () => void;
}

export const Card: React.FC<CardProps> = ({
  children,
  className = '',
  hoverEffect = false,
  onClick,
}) => {
  const hoverClasses = hoverEffect
    ? 'hover:shadow-xl hover:-translate-y-1 transition-all duration-300 cursor-pointer'
    : '';

  return (
    <div
      onClick={onClick}
      className={`bg-white dark:bg-dark-card border border-gray-200/80 dark:border-dark-border rounded-2xl p-6 shadow-sm ${hoverClasses} ${className}`}
    >
      {children}
    </div>
  );
};
