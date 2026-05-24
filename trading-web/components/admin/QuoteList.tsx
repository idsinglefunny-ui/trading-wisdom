/**
 * QuoteList Component - Display and manage quotes in admin
 */

'use client';

import { Quote } from '@/lib/api';
import { CategoryBadge } from '@/components';

interface QuoteListProps {
  quotes: Quote[];
  onEdit: (quote: Quote) => void;
  onDelete: (id: number) => void;
  isLoading?: boolean;
}

export function QuoteList({ quotes, onEdit, onDelete, isLoading }: QuoteListProps) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
          <p className="mt-4 text-muted-foreground">加载中...</p>
        </div>
      </div>
    );
  }

  if (quotes.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <div className="text-6xl mb-4">📭</div>
        <h3 className="text-lg font-medium text-foreground">暂无语录</h3>
        <p className="mt-2 text-sm text-muted-foreground">点击上方"新增语录"按钮添加第一条语录</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {quotes.map((quote) => (
        <div
          key={quote.id}
          className="surface-elevation-1 rounded-lg p-4 transition-shadow hover:shadow-lg"
        >
          <div className="flex items-start justify-between gap-4">
            {/* Content */}
            <div className="flex-1 space-y-3">
              <p className="text-foreground leading-relaxed">"{quote.content}"</p>

              {/* Metadata */}
              <div className="flex flex-wrap items-center gap-3 text-sm">
                <CategoryBadge category={quote.category} />
                <span className="text-muted-foreground">|</span>
                <span className="text-muted-foreground">{quote.marketTypeDisplay}</span>
                {quote.source && (
                  <>
                    <span className="text-muted-foreground">|</span>
                    <span className="text-muted-foreground">来源: {quote.source}</span>
                  </>
                )}
              </div>

              {/* Stats */}
              <div className="flex items-center gap-4 text-xs text-muted-foreground">
                <span>浏览: {quote.viewCount}</span>
                <span>收藏: {quote.favoriteCount}</span>
                <span>ID: {quote.id}</span>
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center space-x-2">
              <button
                onClick={() => onEdit(quote)}
                className="rounded-lg border border-border bg-surface px-3 py-1.5 text-sm font-medium text-foreground transition-colors hover:bg-surface-variant"
                title="编辑"
              >
                编辑
              </button>
              <button
                onClick={() => onDelete(quote.id)}
                className="rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-1.5 text-sm font-medium text-destructive transition-colors hover:bg-destructive/20"
                title="删除"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
