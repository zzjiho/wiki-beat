// Per-minute statistics type
export interface MinuteStats {
  minute: string;
  editCount: number;
  botEditCount: number;
  newPageCount: number;
  minorEditCount: number;
  totalSizeChange: number;
  averageSizeChange: number;
  timestamp: string;
  newPageRatio: number;
  botEditRatio: number;
  minorEditRatio: number;
}

// Core statistics data type
export interface Stats {
  averageEditsPerMinute: number;
  recentMinutes: Record<string, MinuteStats>;
  averageBotRatio: number;
  currentMinuteEdits?: number;
  minuteEditCounts?: Record<string, number>;
  // Total Stats data (cumulative values for ratio calculation)
  totalCount: number;
  totalBotCount: number;
  totalNewPages: number;
  totalMinorEdits: number;
  topLanguages: Record<string, number>;
  totalSizeChange: number;
  lastUpdate: string;
  // Average language distribution per minute
  perMinuteLanguages: Record<string, number>;
}

// Removed unused types:
// - Edit: Real-time edit feed not implemented
// - LanguageStats: Not used anywhere

// Ranking item type
export interface RankingItem {
  rank: number;
  name: string;
  count: number;
  url?: string;
}

// Removed unused types:
// - RankingData: Currently using VandalismStats

// API response type
export interface ApiResponse<T> {
  httpStatus: string;
  message: string;
  data: T;
}

// Language statistics item type
export interface LanguageStatsItem {
  language: string;
  editCount: number;
  botEditCount: number;
  userCount: number;
  pageCount: number;
  wikiDistribution: Record<string, number>;
  hourlyDistribution: Record<string, number>;
  timestamp: string;
  editsPerUser: number;
  botEditRatio: number;
  editsPerPage: number;
}

// Total statistics API response type (new structure)
export interface TotalStatsResponse {
  avgEditsPerMinute: number;
  description: string;
  perMinuteLanguageDistribution: {
    averagePerMinute: Record<string, number>;
    distribution: Record<string, number>;
    totalMinutes: number;
  };
  avgBotRatio: number;
  currentMinuteEdits: number;
  timeWindow: string;
}

// Dashboard API response type (new structure)
export interface DashboardResponse {
  totalEdits: number;
  botRatio: number;
  newPageRatio: number;
  topLanguages: LanguageStatsItem[]; // Changed to array
  minuteChart: Record<string, number>;
}

// Popular document item type
export interface PopularDocument {
  title: string;
  language: string;
  editCount: number;
  lastEditTime: string;
  url: string;
}

// Popular documents data type
export interface PopularDocumentsData {
  globalTop10: PopularDocument[];
  englishTop10: PopularDocument[];
  koreanTop10: PopularDocument[];
  windowMinutes: number;
  timestamp: string;
}

// New document item type
export interface NewDocument {
  title: string;
  language: string;
  creator: string;
  isBot: boolean;
  url: string;
  createdTime: string;
}

// New documents data type
export interface NewDocumentsData {
  totalNewDocuments: number;
  byLanguage: Record<string, number>;
  botCreatedCount: number;
  userCreatedCount: number;
  recentDocuments: NewDocument[];
  windowMinutes: number;
  timestamp: string;
}

// Vandalism revert document type
export interface RevertedDocument {
  title: string;
  language: string;
  revertCount: number;
  url: string;
}

// 최근 리버트 활동 타입
export interface RecentRevert {
  title: string;
  language: string;
  revertType: 'rollback' | 'undo' | 'revert' | 'vandalism';
  user: string;
  comment: string;
  url: string;
  revertTime: string;
}

// Vandalism statistics data type
export interface VandalismStats {
  totalReverts: number;
  byType: {
    rollback: number;
    undo: number;
    revert: number;
    vandalism: number;
  };
  mostRevertedDocuments: RevertedDocument[];
  recentReverts: RecentRevert[];
  windowMinutes: number;
  timestamp: string;
}

// 다크모드 테마 타입
export type Theme = 'light' | 'dark';
