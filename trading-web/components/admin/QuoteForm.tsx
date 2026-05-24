/**
 * QuoteForm Component - Form for creating/editing quotes
 */

'use client';

import { useState, useEffect } from 'react';
import { CategoryOption, MarketTypeOption } from '@/lib/api';

export interface QuoteFormData {
  content: string;
  category: string;
  marketType: string;
  source: string;
}

interface QuoteFormProps {
  initialData?: QuoteFormData;
  onSubmit: (data: QuoteFormData) => Promise<void>;
  onCancel?: () => void;
  submitLabel?: string;
  isLoading?: boolean;
}

// Backend enum values: RISK_MGMT, MINDSET, DISCIPLINE, TECHNICAL
const categories: CategoryOption[] = [
  { value: 'RISK_MGMT', label: '风险管理' },
  { value: 'MINDSET', label: '交易心态' },
  { value: 'DISCIPLINE', label: '交易纪律' },
  { value: 'TECHNICAL', label: '技术分析' },
];

// Backend enum values: STOCK, FUTURES, GENERAL
const marketTypes: MarketTypeOption[] = [
  { value: 'STOCK', label: '股票' },
  { value: 'FUTURES', label: '期货' },
  { value: 'GENERAL', label: '通用' },
];

// Backend enum values: SYSTEM, USER
const sourceTypes: CategoryOption[] = [
  { value: 'USER', label: '用户' },
  { value: 'SYSTEM', label: '系统' },
];

export function QuoteForm({
  initialData,
  onSubmit,
  onCancel,
  submitLabel = '提交',
  isLoading = false,
}: QuoteFormProps) {
  const [formData, setFormData] = useState<QuoteFormData>(
    initialData || {
      content: '',
      category: 'RISK_MGMT',
      marketType: 'GENERAL',
      source: 'USER',
    }
  );

  const [errors, setErrors] = useState<Partial<Record<keyof QuoteFormData, string>>>({});

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (errors[name as keyof QuoteFormData]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof QuoteFormData, string>> = {};

    if (!formData.content.trim()) {
      newErrors.content = '请输入语录内容';
    } else if (formData.content.length < 10) {
      newErrors.content = '语录内容至少需要10个字符';
    } else if (formData.content.length > 500) {
      newErrors.content = '语录内容不能超过500个字符';
    }

    if (!formData.category) {
      newErrors.category = '请选择分类';
    }

    if (!formData.marketType) {
      newErrors.marketType = '请选择市场类型';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    await onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Content Field */}
      <div>
        <label htmlFor="content" className="mb-2 block text-sm font-medium text-foreground">
          语录内容 <span className="text-destructive">*</span>
        </label>
        <textarea
          id="content"
          name="content"
          value={formData.content}
          onChange={handleChange}
          placeholder="请输入交易智慧语录..."
          rows={4}
          className={`w-full rounded-lg border bg-surface px-4 py-3 text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${
            errors.content ? 'border-destructive' : 'border-border'
          }`}
          disabled={isLoading}
        />
        {errors.content && (
          <p className="mt-1 text-sm text-destructive">{errors.content}</p>
        )}
        <p className="mt-1 text-xs text-muted-foreground">
          {formData.content.length} / 500 字符
        </p>
      </div>

      {/* Category Field */}
      <div>
        <label htmlFor="category" className="mb-2 block text-sm font-medium text-foreground">
          分类 <span className="text-destructive">*</span>
        </label>
        <select
          id="category"
          name="category"
          value={formData.category}
          onChange={handleChange}
          className={`w-full rounded-lg border bg-surface px-4 py-2.5 text-foreground focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${
            errors.category ? 'border-destructive' : 'border-border'
          }`}
          disabled={isLoading}
        >
          {categories.map((cat) => (
            <option key={cat.value} value={cat.value}>
              {cat.label}
            </option>
          ))}
        </select>
        {errors.category && (
          <p className="mt-1 text-sm text-destructive">{errors.category}</p>
        )}
      </div>

      {/* Market Type Field */}
      <div>
        <label htmlFor="marketType" className="mb-2 block text-sm font-medium text-foreground">
          市场类型 <span className="text-destructive">*</span>
        </label>
        <select
          id="marketType"
          name="marketType"
          value={formData.marketType}
          onChange={handleChange}
          className={`w-full rounded-lg border bg-surface px-4 py-2.5 text-foreground focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${
            errors.marketType ? 'border-destructive' : 'border-border'
          }`}
          disabled={isLoading}
        >
          {marketTypes.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
        {errors.marketType && (
          <p className="mt-1 text-sm text-destructive">{errors.marketType}</p>
        )}
      </div>

      {/* Source Field */}
      <div>
        <label htmlFor="source" className="mb-2 block text-sm font-medium text-foreground">
          来源类型 <span className="text-destructive">*</span>
        </label>
        <select
          id="source"
          name="source"
          value={formData.source}
          onChange={handleChange}
          className={`w-full rounded-lg border bg-surface px-4 py-2.5 text-foreground focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${
            errors.source ? 'border-destructive' : 'border-border'
          }`}
          disabled={isLoading}
        >
          {sourceTypes.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
        {errors.source && (
          <p className="mt-1 text-sm text-destructive">{errors.source}</p>
        )}
        <p className="mt-1 text-xs text-muted-foreground">
          选择语录来源类型，系统预设语录使用 SYSTEM，用户添加使用 USER
        </p>
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-end space-x-3 pt-4">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="rounded-lg border border-border bg-surface px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-surface-variant disabled:cursor-not-allowed disabled:opacity-50"
          >
            取消
          </button>
        )}
        <button
          type="submit"
          disabled={isLoading}
          className="rounded-lg bg-primary px-6 py-2 text-sm font-medium text-primary-foreground transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isLoading ? '提交中...' : submitLabel}
        </button>
      </div>
    </form>
  );
}
