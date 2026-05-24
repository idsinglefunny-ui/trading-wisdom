/**
 * Footer Component - Trading Quotes Web App
 * Displays copyright information
 */

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full border-t border-border bg-surface py-6">
      <div className="container mx-auto max-w-5xl px-4">
        <div className="flex flex-col items-center justify-center space-y-2 text-center">
          <p className="text-sm text-muted-foreground">
            &copy; {currentYear} 交易智慧. All rights reserved.
          </p>
          <p className="text-xs text-muted-foreground/60">
            精选交易语录，助力投资智慧
          </p>
        </div>
      </div>
    </footer>
  );
}
