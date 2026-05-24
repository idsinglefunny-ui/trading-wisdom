/**
 * Admin Login Page
 * Password-based authentication for admin access
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { adminLogin } from '@/lib/api';
import { ADMIN_TOKEN_KEY } from '@/lib/config';

export default function AdminLoginPage() {
  const router = useRouter();
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const token = await adminLogin(password);
      localStorage.setItem(ADMIN_TOKEN_KEY, token);
      router.push('/internal/manage/61cfc5ffa723/dashboard');
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('登录失败，请检查密码是否正确');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-md space-y-8">
        {/* Logo/Title */}
        <div className="text-center">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-primary">
            <span className="text-3xl font-bold text-primary-foreground">管</span>
          </div>
          <h1 className="mt-6 text-3xl font-bold text-foreground">管理后台登录</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            请输入管理员密码以访问后台
          </p>
        </div>

        {/* Login Form */}
        <div className="surface-elevation-2 rounded-xl p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Password Field */}
            <div>
              <label htmlFor="password" className="mb-2 block text-sm font-medium text-foreground">
                管理员密码
              </label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码"
                className={`w-full rounded-lg border bg-surface px-4 py-3 text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 ${
                  error ? 'border-destructive' : 'border-border'
                }`}
                disabled={isLoading}
                autoFocus
              />
              {error && (
                <p className="mt-2 text-sm text-destructive">{error}</p>
              )}
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading || !password}
              className="w-full rounded-lg bg-primary px-4 py-3 text-base font-medium text-primary-foreground transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isLoading ? '登录中...' : '登录'}
            </button>
          </form>
        </div>

        {/* Back to Home Link */}
        <div className="text-center">
          <a
            href="/"
            className="text-sm font-medium text-primary transition-colors hover:underline"
          >
            返回前台首页
          </a>
        </div>
      </div>
    </div>
  );
}
