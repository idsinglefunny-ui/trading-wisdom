/**
 * About Page - Trading Wisdom (交易智慧)
 * Project introduction and information
 */

import { Container } from '@/components';

export default function AboutPage() {
  return (
    <section className="flex min-h-[calc(100vh-4rem)] items-center py-16 px-4">
      <Container size="lg">
        {/* Page Title */}
        <div className="mb-12 text-center">
          <h1 className="mb-4 text-4xl font-bold tracking-tight text-foreground md:text-5xl">
            关于交易智慧
          </h1>
          <div className="mx-auto h-1 w-24 rounded-full bg-primary" />
        </div>

        {/* Content Cards */}
        <div className="space-y-6">
          {/* Project Introduction Card */}
          <div className="surface-elevation-2 rounded-2xl p-8">
            <div className="mb-4 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/20">
                <svg className="h-5 w-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h2 className="text-2xl font-semibold text-foreground">项目介绍</h2>
            </div>
            <p className="text-lg leading-relaxed text-muted-foreground">
              <strong className="text-foreground">交易智慧</strong> 是一款专注于交易心理和投资智慧的移动应用。
              我们汇集了华尔街投资大师的经典语录，帮助每一位交易者在金融市场的波澜中保持清醒与坚定。
            </p>
          </div>

          {/* Mission Card */}
          <div className="surface-elevation-2 rounded-2xl p-8">
            <div className="mb-4 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/20">
                <svg className="h-5 w-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                </svg>
              </div>
              <h2 className="text-2xl font-semibold text-foreground">项目使命</h2>
            </div>
            <p className="text-lg leading-relaxed text-muted-foreground">
              帮助交易者建立<strong className="text-foreground">正确的交易心态</strong>和<strong className="text-foreground">严格的纪律</strong>。
              在市场狂热时保持冷静，在市场恐慌时坚守原则，让投资智慧成为你交易路上最可靠的伙伴。
            </p>
          </div>

          {/* Features Card */}
          <div className="surface-elevation-2 rounded-2xl p-8">
            <div className="mb-6 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/20">
                <svg className="h-5 w-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                </svg>
              </div>
              <h2 className="text-2xl font-semibold text-foreground">核心功能</h2>
            </div>
            <div className="grid gap-4 sm:grid-cols-3">
              {/* Feature 1 */}
              <div className="rounded-xl bg-surface-container-low p-5">
                <div className="mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-risk-mgmt/20">
                  <svg className="h-4 w-4 text-risk-mgmt" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                </div>
                <h3 className="mb-2 font-semibold text-foreground">语录提醒</h3>
                <p className="text-sm text-muted-foreground">每日定时推送投资智慧，让正确的交易理念时刻伴随</p>
              </div>

              {/* Feature 2 */}
              <div className="rounded-xl bg-surface-container-low p-5">
                <div className="mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-mindset/20">
                  <svg className="h-4 w-4 text-mindset" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                  </svg>
                </div>
                <h3 className="mb-2 font-semibold text-foreground">分类浏览</h3>
                <p className="text-sm text-muted-foreground">按风险管理、心态培养、交易纪律等主题分类查找</p>
              </div>

              {/* Feature 3 */}
              <div className="rounded-xl bg-surface-container-low p-5">
                <div className="mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-discipline/20">
                  <svg className="h-4 w-4 text-discipline" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                </div>
                <h3 className="mb-2 font-semibold text-foreground">收藏管理</h3>
                <p className="text-sm text-muted-foreground">收藏触动心弦的语录，建立属于自己的智慧宝库</p>
              </div>
            </div>
          </div>
        </div>
      </Container>
    </section>
  );
}
