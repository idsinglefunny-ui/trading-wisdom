/**
 * Home Page - Trading Wisdom (交易智慧)
 * Minimalist hero page with centered content
 */

import Link from 'next/link';
import { Container } from '@/components';

export default function HomePage() {
  return (
    /* Hero Section - Full viewport height with centered content */
    <section className="flex min-h-[calc(100vh-4rem)] flex-col items-center justify-center px-4">
        <Container size="lg" className="text-center">
          {/* Brand Name */}
          <h1 className="mb-6 text-6xl font-bold tracking-tight text-foreground md:text-7xl">
            交易智慧
          </h1>

          {/* Subtitle */}
          <p className="mb-12 text-xl text-muted-foreground md:text-2xl">
            让交易智慧常伴左右
          </p>

          {/* CTA Buttons */}
          <div className="flex flex-col items-center gap-4 sm:flex-row sm:justify-center">
            {/* Download App Button - Primary */}
            <a
              href="/app-release.apk"
              download
              className="button-primary inline-flex items-center gap-2 text-base px-8 py-4"
            >
              <svg
                className="h-5 w-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                />
              </svg>
              下载 App
            </a>

            {/* Browse Quotes Link - Ghost Style */}
            <Link
              href="/quotes"
              className="button-ghost inline-flex items-center gap-2 text-base px-8 py-4"
            >
              <svg
                className="h-5 w-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
              浏览语录
            </Link>
          </div>

          {/* App Store Badges - Coming Soon */}
          <div className="mt-16 text-center">
            <p className="text-sm text-muted-foreground">即将上线</p>
            <div className="mt-4 flex items-center justify-center gap-4 opacity-50">
              <div className="flex h-12 items-center gap-2 rounded-lg border border-border px-4">
                <svg className="h-6 w-6" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z" />
                </svg>
                <span className="text-sm">App Store</span>
              </div>
              <div className="flex h-12 items-center gap-2 rounded-lg border border-border px-4">
                <svg className="h-6 w-6" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 01-.61-.92V2.734a1 1 0 01.609-.92zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.198l2.807 1.626a1 1 0 010 1.73l-2.808 1.626L15.206 12l2.492-2.491zM5.864 2.658L16.8 8.99l-2.302 2.302-8.634-8.634z" />
                </svg>
                <span className="text-sm">Google Play</span>
              </div>
            </div>
          </div>
        </Container>
      </section>
  );
}
