/**
 * QuoteCard Component - Trading Quotes Web App
 * Displays a single quote with optional actions
 */

import { Quote } from '@/lib/api';
import { CategoryBadge } from './CategoryBadge';
import { Icon } from './Icon';

interface QuoteCardProps {
  quote: Quote;
  showActions?: boolean;
  onFavorite?: (quoteId: number) => void;
  onShare?: (quote: Quote) => void;
  className?: string;
}

export function QuoteCard({
  quote,
  showActions = false,
  onFavorite,
  onShare,
  className = '',
}: QuoteCardProps) {
  return (
    <article
      className={`group relative overflow-hidden rounded-material-lg border border-border bg-card p-6 shadow-elevation-1 transition-all hover:shadow-elevation-2 ${className}`}
    >
      {/* Category Badge */}
      <div className="mb-3">
        <CategoryBadge category={quote.category} />
      </div>

      {/* Quote Content */}
      <blockquote className="mb-4">
        <p className="text-lg leading-relaxed text-card-foreground">
          &ldquo;{quote.content}&rdquo;
        </p>
      </blockquote>

      {/* Quote Metadata */}
      <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
        {/* Source */}
        {quote.source && (
          <div className="flex items-center">
            <Icon name="source" className="mr-1.5" />
            <span>{quote.source}</span>
          </div>
        )}

        {/* Market Type */}
        <div className="flex items-center">
          <Icon name="marketType" className="mr-1.5" />
          <span>{quote.marketTypeDisplay}</span>
        </div>

        {/* View Count */}
        {quote.viewCount > 0 && (
          <div className="flex items-center">
            <Icon name="viewCount" className="mr-1.5" />
            <span>{quote.viewCount}</span>
          </div>
        )}

        {/* Favorite Indicator */}
        {quote.isFavorite && (
          <div className="flex items-center">
            <Icon name="favorite" className="text-warning" filled />
          </div>
        )}
      </div>

      {/* Action Buttons */}
      {showActions && (
        <div className="mt-4 flex items-center gap-2 pt-4 opacity-0 transition-opacity group-hover:opacity-100">
          {/* Favorite Button */}
          {onFavorite && (
            <button
              onClick={() => onFavorite(quote.id)}
              className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-surface hover:text-foreground"
              aria-label={quote.isFavorite ? '取消收藏' : '收藏'}
            >
              <Icon
                name="favorite"
                className={quote.isFavorite ? 'fill-warning text-warning' : ''}
                filled={quote.isFavorite}
              />
              <span>{quote.isFavorite ? '已收藏' : '收藏'}</span>
            </button>
          )}

          {/* Share Button */}
          {onShare && (
            <button
              onClick={() => onShare(quote)}
              className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-surface hover:text-foreground"
              aria-label="分享"
            >
              <Icon name="share" />
              <span>分享</span>
            </button>
          )}
        </div>
      )}
    </article>
  );
}
