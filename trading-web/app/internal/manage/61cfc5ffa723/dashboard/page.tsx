/**
 * Admin Dashboard Page
 * Main admin interface for managing quotes
 */

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { AdminLayout, QuoteForm, QuoteList, QuoteFormData } from '@/components/admin';
import { Quote, getAdminQuotes, createQuote, updateQuote, deleteQuote } from '@/lib/api';
import { ADMIN_TOKEN_KEY } from '@/lib/config';

type FormMode = 'create' | 'edit' | null;

export default function AdminDashboardPage() {
  const router = useRouter();
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>(null);
  const [editingQuote, setEditingQuote] = useState<Quote | null>(null);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Load quotes on mount
  useEffect(() => {
    loadQuotes();
  }, []);

  const getToken = (): string => {
    const token = localStorage.getItem(ADMIN_TOKEN_KEY);
    if (!token) {
      router.push('/internal/manage/61cfc5ffa723/login');
      throw new Error('No auth token');
    }
    return token;
  };

  const loadQuotes = async () => {
    try {
      setIsLoading(true);
      setError('');
      const token = getToken();
      const data = await getAdminQuotes(token);
      setQuotes(data);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('加载失败，请重新登录');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = async (data: QuoteFormData) => {
    try {
      setIsSubmitting(true);
      setError('');
      const token = getToken();
      const newQuote = await createQuote(token, data);
      setQuotes([...quotes, newQuote]);
      setFormMode(null);
      setSuccessMessage('创建成功！');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('创建失败');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdate = async (id: number, data: QuoteFormData) => {
    try {
      setIsSubmitting(true);
      setError('');
      const token = getToken();
      const updatedQuote = await updateQuote(token, id, data);
      setQuotes(quotes.map(q => q.id === id ? updatedQuote : q));
      setFormMode(null);
      setEditingQuote(null);
      setSuccessMessage('更新成功！');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('更新失败');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除这条语录吗？')) return;

    try {
      setIsSubmitting(true);
      setError('');
      const token = getToken();
      await deleteQuote(token, id);
      setQuotes(quotes.filter(q => q.id !== id));
      setSuccessMessage('删除成功！');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('删除失败');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (quote: Quote) => {
    setEditingQuote(quote);
    setFormMode('edit');
  };

  const handleCancelEdit = () => {
    setFormMode(null);
    setEditingQuote(null);
  };

  const handleLogout = () => {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    router.push('/internal/manage/61cfc5ffa723/login');
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">语录管理</h1>
            <p className="text-sm text-muted-foreground">
              共 {quotes.length} 条语录
            </p>
          </div>
          <button
            onClick={() => setFormMode('create')}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:opacity-90"
          >
            添加语录
          </button>
        </div>

        {/* Messages */}
        {error && (
          <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive">
            {error}
          </div>
        )}
        {successMessage && (
          <div className="rounded-lg border border-green-500/50 bg-green-500/10 p-3 text-sm text-green-600 dark:text-green-400">
            {successMessage}
          </div>
        )}

        {/* Form */}
        {(formMode === 'create' || formMode === 'edit') && (
          <div className="rounded-lg border border-border bg-card p-6">
            <h2 className="mb-4 text-lg font-semibold text-foreground">
              {formMode === 'create' ? '添加新语录' : '编辑语录'}
            </h2>
            <QuoteForm
              initialData={editingQuote ? {
                content: editingQuote.content,
                category: editingQuote.category,
                marketType: editingQuote.marketType,
                source: editingQuote.source,
              } : undefined}
              onSubmit={formMode === 'create' ? handleCreate : (data) => handleUpdate(editingQuote!.id, data)}
              onCancel={handleCancelEdit}
              submitLabel={formMode === 'create' ? '添加' : '保存'}
              isLoading={isSubmitting}
            />
          </div>
        )}

        {/* Quote List */}
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          </div>
        ) : quotes.length === 0 ? (
          <div className="rounded-lg border border-dashed border-border p-12 text-center">
            <p className="text-muted-foreground">暂无语录，点击上方按钮添加</p>
          </div>
        ) : (
          <QuoteList
            quotes={quotes}
            onEdit={handleEdit}
            onDelete={handleDelete}
          />
        )}
      </div>
    </AdminLayout>
  );
}
