/**
 * Header Component - Trading Quotes Web App
 * Displays logo/title and navigation links
 */

import Link from 'next/link';

const navLinks = [
  { href: '/', label: '首页' },
  { href: '/quotes', label: '语录浏览' },
  { href: '/about', label: '关于' },
] as const;

export function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-16 max-w-5xl items-center justify-between px-4">
        {/* Logo/Title */}
        <Link href="/" className="flex items-center space-x-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
            <span className="text-sm font-bold text-primary-foreground">交</span>
          </div>
          <span className="text-lg font-semibold text-foreground">交易智慧</span>
        </Link>

        {/* Navigation Links */}
        <nav className="flex items-center space-x-6">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className="text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
            >
              {link.label}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  );
}
