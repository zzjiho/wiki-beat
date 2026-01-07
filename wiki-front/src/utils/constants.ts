// API endpoints
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888';

export const API_ENDPOINTS = {
  STATS_TOTAL: '/api/v1/bigdata/stats/total',
  DASHBOARD: '/api/v1/bigdata/dashboard',
  POPULAR_DOCUMENTS: '/api/v1/bigdata/popular-documents',
  NEW_DOCUMENTS: '/api/v1/bigdata/new-documents',
  VANDALISM_STATS: '/api/v1/bigdata/vandalism-stats',
} as const;

// Chart colors
export const CHART_COLORS = [
  '#0ea5e9',
  '#8b5cf6',
  '#ec4899',
  '#f59e0b',
  '#10b981',
  '#6366f1',
  '#f97316',
  '#14b8a6',
] as const;

// Refresh intervals (ms)
export const REFRESH_INTERVALS = {
  STATS: 60000,       // 1 minute (aligned with backend stats refresh cycle)
  POPULAR_DOCUMENTS: 60000, // 1 minute (5-minute aggregation data, refreshed every minute)
  NEW_DOCUMENTS: 60000, // 1 minute (5-minute aggregation data, refreshed every minute)
  VANDALISM_STATS: 60000, // 1 minute (5-minute aggregation data, refreshed every minute)
} as const;

// Removed unused constants:
// - MAX_FEED_ITEMS: Real-time feed not implemented
