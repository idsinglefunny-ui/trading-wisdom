/**
 * API Client for Trading Quotes Web App
 * Connects to Go backend API
 */

// API Base URL - use relative path for production, or override with env var
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '';

// ============================================================================
// Type Definitions
// ============================================================================

export interface Quote {
  id: number;
  content: string;
  category: string;
  categoryDisplay: string;
  marketType: string;
  marketTypeDisplay: string;
  source: string;
  isFavorite: boolean;
  favoriteCount: number;
  viewCount: number;
  createdAt: number;
}

export interface Option {
  value: string;
  label: string;
}

export type CategoryOption = Option;
export type MarketTypeOption = Option;

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// Query parameters for quotes list
export interface QuotesQueryParams {
  offset?: number;
  limit?: number;
  category?: string;
  marketType?: string;
  keyword?: string;
  source?: string;
}

// Query parameters for random quote
export interface RandomQuoteParams {
  category?: string;
  marketType?: string;
}

// Response for paginated quotes list
export interface QuotesListResponse {
  quotes: Quote[];
  total: number;
  offset: number;
  limit: number;
}

// ============================================================================
// API Error Handling
// ============================================================================

export class ApiError extends Error {
  constructor(
    public code: number,
    public message: string,
    public statusCode?: number
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// ============================================================================
// Internal Helper Functions
// ============================================================================

/**
 * Build query string from params object
 */
function buildQueryString(params?: Record<string, unknown> | QuotesQueryParams | RandomQuoteParams): string {
  if (!params) return '';

  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value));
    }
  }

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : '';
}

/**
 * Perform fetch request with error handling
 */
async function fetchApi<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    // Handle non-JSON responses (e.g., 404, 500 error pages)
    const contentType = response.headers.get('content-type');
    if (!contentType?.includes('application/json')) {
      throw new ApiError(
        0,
        `Invalid response from server: ${response.statusText}`,
        response.status
      );
    }

    const data: ApiResponse<T> = await response.json();

    // Check if API returned an error code
    if (data.code !== 0) {
      throw new ApiError(data.code, data.message, response.status);
    }

    return data.data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    // Network errors or other issues
    throw new ApiError(0, error instanceof Error ? error.message : 'Unknown error');
  }
}

// ============================================================================
// Public API Functions
// ============================================================================

/**
 * Get quotes list with optional filters
 *
 * @param params - Query parameters for filtering and pagination
 * @returns Promise resolving to quotes list response
 *
 * @example
 * // Get first page of quotes
 * const result = await getQuotes({ offset: 0, limit: 20 });
 *
 * @example
 * // Get filtered quotes
 * const result = await getQuotes({
 *   category: 'strategy',
 *   marketType: 'crypto'
 * });
 */
export async function getQuotes(
  params?: QuotesQueryParams
): Promise<QuotesListResponse> {
  const queryString = buildQueryString(params);
  return fetchApi<QuotesListResponse>(`/api/quotes${queryString}`);
}

/**
 * Get a random quote
 *
 * @param params - Optional filters for category or market type
 * @returns Promise resolving to a random quote
 *
 * @example
 * // Get completely random quote
 * const quote = await getRandomQuote();
 *
 * @example
 * // Get random crypto quote
 * const quote = await getRandomQuote({ marketType: 'crypto' });
 */
export async function getRandomQuote(
  params?: RandomQuoteParams
): Promise<Quote> {
  const queryString = buildQueryString(params);
  return fetchApi<Quote>(`/api/quotes/random${queryString}`);
}

/**
 * Get a specific quote by ID
 *
 * @param id - Quote ID
 * @returns Promise resolving to the quote
 *
 * @example
 * const quote = await getQuoteById(123);
 */
export async function getQuoteById(id: number): Promise<Quote> {
  return fetchApi<Quote>(`/api/quotes/${encodeURIComponent(id)}`);
}

/**
 * Get all available categories
 *
 * @returns Promise resolving to categories list
 *
 * @example
 * const categories = await getCategories();
 * // Returns: [{ value: 'strategy', label: '策略思维' }, ...]
 */
export async function getCategories(): Promise<Option[]> {
  return fetchApi<Option[]>('/api/quotes/categories');
}

/**
 * Get all available market types
 *
 * @returns Promise resolving to market types list
 *
 * @example
 * const marketTypes = await getMarketTypes();
 * // Returns: [{ value: 'stock', label: '股票' }, ...]
 */
export async function getMarketTypes(): Promise<Option[]> {
  return fetchApi<Option[]>('/api/quotes/market-types');
}

/**
 * Get system preset quotes
 *
 * @returns Promise resolving to system quotes list
 *
 * @example
 * const systemQuotes = await getSystemQuotes();
 */
export async function getSystemQuotes(): Promise<Quote[]> {
  return fetchApi<Quote[]>('/api/system/quotes');
}

// ============================================================================
// Admin API Types and Functions
// ============================================================================

export interface AdminLoginRequest {
  password: string;
}

export interface AdminLoginResponse {
  token: string;
}

export interface CreateQuoteRequest {
  content: string;
  category: string;
  marketType: string;
  source?: string;
}

export interface UpdateQuoteRequest {
  content?: string;
  category?: string;
  marketType?: string;
  source?: string;
}

/**
 * Admin login with password
 *
 * @param password - Admin password
 * @returns Promise resolving to auth token
 *
 * @example
 * const token = await adminLogin('your-password');
 */
export async function adminLogin(password: string): Promise<string> {
  const response = await fetchApi<AdminLoginResponse>('/api/admin/login', {
    method: 'POST',
    body: JSON.stringify({ password } as AdminLoginRequest),
  });
  return response.token;
}

/**
 * Get all quotes for admin (no pagination, includes all)
 *
 * @param token - Admin auth token
 * @returns Promise resolving to all quotes
 *
 * @example
 * const quotes = await getAdminQuotes(token);
 */
export async function getAdminQuotes(token: string): Promise<Quote[]> {
  const response = await fetchApi<{quotes: Quote[], total: number, limit: number, offset: number}>('/api/admin/quotes', {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });
  return response.quotes;
}

/**
 * Create a new quote
 *
 * @param token - Admin auth token
 * @param data - Quote data
 * @returns Promise resolving to created quote
 *
 * @example
 * const quote = await createQuote(token, {
 *   content: '新的交易智慧',
 *   category: 'strategy',
 *   marketType: 'stock',
 *   source: '巴菲特'
 * });
 */
export async function createQuote(
  token: string,
  data: CreateQuoteRequest
): Promise<Quote> {
  return fetchApi<Quote>('/api/admin/quotes', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });
}

/**
 * Update an existing quote
 *
 * @param token - Admin auth token
 * @param id - Quote ID
 * @param data - Updated quote data
 * @returns Promise resolving to updated quote
 *
 * @example
 * const quote = await updateQuote(token, 123, {
 *   content: '更新后的内容'
 * });
 */
export async function updateQuote(
  token: string,
  id: number,
  data: UpdateQuoteRequest
): Promise<Quote> {
  return fetchApi<Quote>(`/api/admin/quotes/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });
}

/**
 * Delete a quote
 *
 * @param token - Admin auth token
 * @param id - Quote ID
 * @returns Promise that resolves when deleted
 *
 * @example
 * await deleteQuote(token, 123);
 */
export async function deleteQuote(token: string, id: number): Promise<void> {
  await fetchApi<void>(`/api/admin/quotes/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });
}

// ============================================================================
// Re-exports for convenience
// ============================================================================

export const api = {
  getQuotes,
  getRandomQuote,
  getQuoteById,
  getCategories,
  getMarketTypes,
  getSystemQuotes,
};

export default api;
