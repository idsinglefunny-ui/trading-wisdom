/**
 * CategoryBadge Component - Trading Quotes Web App
 * Displays a category badge with configured colors
 */

import { getCategoryConfig } from '@/lib/categories';

interface CategoryBadgeProps {
  category: string;
  className?: string;
}

export function CategoryBadge({ category, className = '' }: CategoryBadgeProps) {
  const config = getCategoryConfig(category);

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors ${className}`}
      style={{
        color: config.color,
        backgroundColor: config.bgColor,
        borderColor: config.borderColor,
      }}
    >
      {config.label}
    </span>
  );
}
