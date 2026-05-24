/**
 * Category configuration for Trading Quotes Web App
 * Defines category colors and display names
 * Must match backend enum values in trading-api/models/quote.go
 */

export interface CategoryConfig {
  value: string;
  label: string;
  color: string;
  bgColor: string;
  borderColor: string;
}

/**
 * Category color mapping based on Material 3 theme
 * Backend enum values: RISK_MGMT, MINDSET, DISCIPLINE, TECHNICAL
 */
export const categoryColors: Record<string, CategoryConfig> = {
  'RISK_MGMT': {
    value: 'RISK_MGMT',
    label: '风险管理',
    color: '#0EA5E9',
    bgColor: 'rgba(14, 165, 233, 0.15)',
    borderColor: 'rgba(14, 165, 233, 0.3)',
  },
  'MINDSET': {
    value: 'MINDSET',
    label: '交易心态',
    color: '#F59E0B',
    bgColor: 'rgba(245, 158, 11, 0.15)',
    borderColor: 'rgba(245, 158, 11, 0.3)',
  },
  'DISCIPLINE': {
    value: 'DISCIPLINE',
    label: '交易纪律',
    color: '#10B981',
    bgColor: 'rgba(16, 185, 129, 0.15)',
    borderColor: 'rgba(16, 185, 129, 0.3)',
  },
  'TECHNICAL': {
    value: 'TECHNICAL',
    label: '技术分析',
    color: '#8B5CF6',
    bgColor: 'rgba(139, 92, 246, 0.15)',
    borderColor: 'rgba(139, 92, 246, 0.3)',
  },
};

/**
 * Get category configuration by value
 */
export function getCategoryConfig(category: string): CategoryConfig {
  return categoryColors[category] || {
    value: category,
    label: category,
    color: '#94A3B8',
    bgColor: 'rgba(148, 163, 184, 0.15)',
    borderColor: 'rgba(148, 163, 184, 0.3)',
  };
}

/**
 * Get category badge class name for CSS
 */
export function getCategoryBadgeClass(category: string): string {
  const validCategories = ['RISK_MGMT', 'MINDSET', 'DISCIPLINE', 'TECHNICAL'];
  if (validCategories.includes(category)) {
    return `category-badge-${category}`;
  }
  return 'category-badge-default';
}
