import axios, { AxiosError } from 'axios';
import { API_BASE_URL, API_ENDPOINTS } from '../utils/constants';
import type { Stats, ApiResponse, TotalStatsResponse, DashboardResponse, MinuteStats, PopularDocumentsData, NewDocumentsData, VandalismStats } from '../types';

/**
 * Create Axios instance
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * API error type definition
 */
export interface ApiError {
  message: string;
  status?: number;
  code?: string;
}

/**
 * Response interceptor (error handling)
 *
 * Responsibilities:
 * 1. Classify HTTP error codes (4xx, 5xx)
 * 2. Handle network errors (timeout, offline)
 * 3. Generate user-friendly error messages
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    let errorMessage = 'An unknown error occurred';

    if (error.response) {
      // Server responded with 4xx, 5xx errors
      const status = error.response.status;

      switch (status) {
        case 400:
          errorMessage = 'Bad request';
          break;
        case 401:
          errorMessage = 'Authentication required';
          break;
        case 403:
          errorMessage = 'Access denied';
          break;
        case 404:
          errorMessage = 'Requested data not found';
          break;
        case 500:
          errorMessage = 'Internal server error';
          break;
        case 502:
        case 503:
          errorMessage = 'Server temporarily unavailable';
          break;
        default:
          errorMessage = `Server error (${status})`;
      }
    } else if (error.request) {
      // Request sent but no response received
      if (error.code === 'ECONNABORTED') {
        errorMessage = 'Request timeout';
      } else if (error.code === 'ERR_NETWORK') {
        errorMessage = 'Network connection failed';
      } else {
        errorMessage = 'Unable to communicate with server';
      }
    }

    if (import.meta.env.DEV) {
      console.error('API Error:', {
        message: errorMessage,
        error,
        url: error.config?.url,
        status: error.response?.status,
      });
    }

    // Return structured error object
    const apiError: ApiError = {
      message: errorMessage,
      status: error.response?.status,
      code: error.code,
    };

    return Promise.reject(apiError);
  }
);

// Fetch core statistics
export const fetchStats = async (): Promise<Stats> => {
  // Call two APIs in parallel
  const [dashboardRes, totalRes] = await Promise.all([
    apiClient.get<ApiResponse<DashboardResponse>>(API_ENDPOINTS.DASHBOARD),
    apiClient.get<ApiResponse<TotalStatsResponse>>(`${API_ENDPOINTS.STATS_TOTAL}?minutes=10`),
  ]);

  const dashboard = dashboardRes.data.data;
  const total = totalRes.data.data;

  // Transform minuteChart to recentMinutes format
  const recentMinutes: Record<string, MinuteStats> = {};
  Object.entries(dashboard.minuteChart).forEach(([timeKey, editCount]) => {
    // Transform "2025-11-03:21:43" to "2025-11-03T21:43:00"
    const parts = timeKey.split(':');
    const datePart = parts[0]; // "2025-11-03"
    const hourPart = parts[1];   // "21"
    const minutePart = parts[2]; // "43"
    const minute = `${datePart}T${hourPart}:${minutePart}:00`;

    recentMinutes[timeKey] = {
      minute,
      editCount,
      botEditCount: Math.round(editCount * dashboard.botRatio / 100),
      newPageCount: Math.round(editCount * dashboard.newPageRatio / 100),
      minorEditCount: 0,
      totalSizeChange: 0,
      averageSizeChange: 0,
      timestamp: minute,
      newPageRatio: dashboard.newPageRatio,
      botEditRatio: dashboard.botRatio,
      minorEditRatio: 0,
    };
  });

  // Average edit count (provided by total API)
  const averageEditsPerMinute = total.avgEditsPerMinute;

  // Transform topLanguages array to Record<string, number>
  const topLanguagesMap: Record<string, number> = {};
  dashboard.topLanguages.forEach(item => {
    topLanguagesMap[item.language] = item.editCount;
  });

  // Calculate based on total edit count
  const totalEdits = dashboard.totalEdits;
  const totalBotCount = Math.round(totalEdits * dashboard.botRatio / 100);
  const totalNewPages = Math.round(totalEdits * dashboard.newPageRatio / 100);

  return {
    averageEditsPerMinute,
    recentMinutes,
    averageBotRatio: total.avgBotRatio,
    currentMinuteEdits: total.currentMinuteEdits,
    minuteEditCounts: dashboard.minuteChart,
    // Utilize Dashboard data (cumulative values for ratio calculation)
    totalCount: totalEdits,
    totalBotCount,
    totalNewPages,
    totalMinorEdits: 0, // Not provided by new API
    topLanguages: topLanguagesMap,
    totalSizeChange: 0, // Not provided by new API
    lastUpdate: new Date().toISOString(),
    // Average language distribution per minute
    perMinuteLanguages: total.perMinuteLanguageDistribution.averagePerMinute,
  };
};

// Fetch popular documents
export const fetchPopularDocuments = async (): Promise<PopularDocumentsData> => {
  const response = await apiClient.get<ApiResponse<PopularDocumentsData>>(API_ENDPOINTS.POPULAR_DOCUMENTS);
  return response.data.data;
};

// Fetch new documents
export const fetchNewDocuments = async (): Promise<NewDocumentsData> => {
  const response = await apiClient.get<ApiResponse<NewDocumentsData>>(API_ENDPOINTS.NEW_DOCUMENTS);
  return response.data.data;
};

// Fetch vandalism statistics
export const fetchVandalismStats = async (): Promise<VandalismStats> => {
  const response = await apiClient.get<ApiResponse<VandalismStats>>(API_ENDPOINTS.VANDALISM_STATS);
  return response.data.data;
};
