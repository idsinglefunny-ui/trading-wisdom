'use client';

/**
 * Quote Browsing Page - Trading Quotes Web App
 * Single-quote focused layout similar to the mobile app experience
 */

import { useState, useEffect, useCallback, useMemo } from 'react';
import { getRandomQuote, Quote } from '@/lib/api';
import { getCategoryConfig, categoryColors } from '@/lib/categories';
import { Container } from '@/components/Container';

export default function QuotesPage() {
  const [currentQuote, setCurrentQuote] = useState<Quote | null>(null);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Build category options dynamically from categoryColors
  const CATEGORIES = useMemo(() => [
    { value: '', label: '全部' },
    ...Object.entries(categoryColors).map(([key, val]) => ({
      value: key,
      label: val.label
    }))
  ], []);

  // Fetch random quote - wrapped in useCallback to fix ESLint dependency warning
  const fetchRandomQuote = useCallback(async (showRefreshAnimation = false) => {
    try {
      if (showRefreshAnimation) {
        setIsRefreshing(true);
      } else {
        setIsLoading(true);
      }

      const params = selectedCategory ? { category: selectedCategory } : undefined;
      const quote = await getRandomQuote(params);
      setCurrentQuote(quote);
    } catch (error) {
      console.error('Failed to fetch quote:', error);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [selectedCategory]);

  // Initial fetch
  useEffect(() => {
    fetchRandomQuote();
  }, [fetchRandomQuote]);

  const handleCategoryChange = (categoryValue: string) => {
    setSelectedCategory(categoryValue);
  };

  const handleRefresh = () => {
    fetchRandomQuote(true);
  };

  // Memoize category config to avoid repeated calls during render
  const categoryConfig = useMemo(() => {
    if (!currentQuote) return null;
    return getCategoryConfig(currentQuote.category);
  }, [currentQuote?.category]);

  return (
    <Container className="py-8">
      <div className="mx-auto max-w-2xl">
        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="mb-2 text-3xl font-bold text-foreground">
            交易语录
          </h1>
          <p className="text-muted-foreground">
            每一条语录都是交易智慧的结晶
          </p>
        </div>

        {/* Category Filter Tabs */}
        <div className="mb-6 flex flex-wrap justify-center gap-2">
          {CATEGORIES.map((category) => {
            const isActive = selectedCategory === category.value;
            return (
              <button
                key={category.value}
                onClick={() => handleCategoryChange(category.value)}
                className={`rounded-full px-4 py-2 text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-surface text-muted-foreground hover:bg-surface-variant hover:text-foreground'
                }`}
                aria-pressed={isActive}
              >
                {category.label}
              </button>
            );
          })}
        </div>

        {/* Main Quote Display */}
        <div className="relative min-h-[300px] rounded-material-xl border border-border bg-card p-8 shadow-elevation-2">
          {isLoading && !currentQuote ? (
            // Initial loading state
            <div className="flex min-h-[300px] items-center justify-center">
              <div className="flex flex-col items-center gap-4">
                <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                <p className="text-sm text-muted-foreground">加载中...</p>
              </div>
            </div>
          ) : currentQuote && categoryConfig ? (
            // Quote content
            <div
              className={`transition-opacity duration-300 ${
                isRefreshing ? 'opacity-50' : 'opacity-100'
              }`}
            >
              {/* Category Badge */}
              <div className="mb-4">
                <span
                  className="inline-flex items-center rounded-full px-3 py-1 text-sm font-medium"
                  style={{
                    color: categoryConfig.color,
                    backgroundColor: categoryConfig.bgColor,
                    borderColor: categoryConfig.borderColor,
                    borderWidth: '1px',
                  }}
                >
                  {currentQuote.categoryDisplay}
                </span>
              </div>

              {/* Quote Content - Large and Centered */}
              <blockquote className="mb-6 text-center">
                <p className="text-2xl font-medium leading-relaxed text-card-foreground sm:text-3xl">
                  "{currentQuote.content}"
                </p>
              </blockquote>

              {/* Quote Metadata */}
              <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
                {/* Source */}
                {currentQuote.source && (
                  <div className="flex items-center">
                    <svg
                      className="mr-1.5 h-4 w-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                      />
                    </svg>
                    <span>{currentQuote.source}</span>
                  </div>
                )}

                {/* Market Type */}
                <div className="flex items-center">
                  <svg
                    className="mr-1.5 h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
                    />
                  </svg>
                  <span>{currentQuote.marketTypeDisplay}</span>
                </div>

                {/* View Count */}
                {currentQuote.viewCount > 0 && (
                  <div className="flex items-center">
                    <svg
                      className="mr-1.5 h-4 w-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                      />
                    </svg>
                    <span>{currentQuote.viewCount}</span>
                  </div>
                )}

                {/* Favorite Indicator */}
                {currentQuote.isFavorite && (
                  <div className="flex items-center">
                    <svg
                      className="h-4 w-4 text-warning"
                      fill="currentColor"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
                      />
                    </svg>
                  </div>
                )}
              </div>

              {/* Refreshing indicator */}
              {isRefreshing && (
                <div className="absolute inset-0 flex items-center justify-center rounded-material-xl bg-card/80">
                  <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                </div>
              )}
            </div>
          ) : (
            // Error state
            <div className="flex min-h-[300px] items-center justify-center">
              <div className="text-center">
                <p className="text-muted-foreground">加载失败，请稍后重试</p>
                <button
                  onClick={handleRefresh}
                  className="mt-4 rounded-full bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:opacity-90"
                >
                  重试
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Actions */}
        <div className="mt-6 flex flex-col items-center gap-4 sm:flex-row sm:justify-between">
          {/* Category Display */}
          {currentQuote && categoryConfig && (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <span>分类:</span>
              <span
                className="font-medium"
                style={{
                  color: categoryConfig.color,
                }}
              >
                {currentQuote.categoryDisplay}
              </span>
            </div>
          )}

          {/* Refresh Button */}
          <button
            onClick={handleRefresh}
            disabled={isLoading || isRefreshing}
            className="flex items-center gap-2 rounded-full bg-primary px-6 py-3 font-medium text-primary-foreground transition-all hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <svg
              className={`h-5 w-5 ${isRefreshing ? 'animate-spin' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
            <span>换一换</span>
          </button>

          {/* Favorite Count */}
          {currentQuote && (
            <div className="flex items-center gap-2 text-sm">
              <span className="text-lg">❤️</span>
              <span className="font-medium text-foreground">{currentQuote.favoriteCount}</span>
            </div>
          )}
        </div>

        {/* Keyboard Hint */}
        <div className="mt-8 text-center">
          <p className="text-xs text-muted-foreground">
            提示：点击"换一换"按钮或切换分类标签来浏览更多语录
          </p>
        </div>
      </div>
    </Container>
  );
}
