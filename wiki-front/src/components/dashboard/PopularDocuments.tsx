import { useState } from 'react';
import { TrendingUp, ExternalLink, Clock, Edit3 } from 'lucide-react';
import { usePopularDocuments } from '../../hooks/queries';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { formatRelativeTime, formatUTCTime } from '../../utils/formatters';
import { getLanguageName, getLanguageBadgeColor } from '../../utils/languages';
import type { PopularDocument } from '../../types';

type TabType = 'global' | 'english' | 'korean';

interface TabConfig {
  id: TabType;
  label: string;
  emoji: string;
}

const TABS: TabConfig[] = [
  { id: 'global', label: 'Global', emoji: '🌍' },
  { id: 'english', label: 'English', emoji: '🔤' },
  { id: 'korean', label: 'Korean', emoji: '🇰🇷' },
];

// Medal emoji by rank
const getRankEmoji = (rank: number): string => {
  switch (rank) {
    case 1: return '🥇';
    case 2: return '🥈';
    case 3: return '🥉';
    default: return `${rank}`;
  }
};

// Background color by rank
const getRankBgColor = (rank: number): string => {
  switch (rank) {
    case 1: return 'bg-gradient-to-br from-yellow-100 to-yellow-200 dark:from-yellow-900/40 dark:to-yellow-800/40';
    case 2: return 'bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700/40 dark:to-gray-600/40';
    case 3: return 'bg-gradient-to-br from-orange-100 to-orange-200 dark:from-orange-900/40 dark:to-orange-800/40';
    default: return 'bg-gray-50 dark:bg-gray-700/30';
  }
};

// Border color by rank
const getRankBorderColor = (rank: number): string => {
  switch (rank) {
    case 1: return 'border-yellow-300 dark:border-yellow-700';
    case 2: return 'border-gray-300 dark:border-gray-600';
    case 3: return 'border-orange-300 dark:border-orange-700';
    default: return 'border-gray-200 dark:border-gray-700';
  }
};

// Remove duplication: Use centralized function from utils/languages.ts

interface DocumentCardProps {
  document: PopularDocument;
  rank: number;
}

const DocumentCard = ({ document, rank }: DocumentCardProps) => {
  return (
    <a
      href={document.url}
      target="_blank"
      rel="noopener noreferrer"
      className={`group block p-4 rounded-lg border-2 transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5 ${getRankBgColor(rank)} ${getRankBorderColor(rank)} hover:border-primary-400 dark:hover:border-primary-500`}
    >
      <div className="flex items-start gap-3">
        {/* Rank badge */}
        <div className="flex-shrink-0">
          <div className={`flex items-center justify-center w-10 h-10 rounded-full text-lg font-bold ${rank <= 3 ? 'text-2xl' : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300'}`}>
            {getRankEmoji(rank)}
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          {/* Title */}
          <div className="flex items-start justify-between gap-2 mb-2">
            <h4 className="text-sm font-semibold text-gray-900 dark:text-white line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {document.title}
            </h4>
            <ExternalLink className="w-4 h-4 text-gray-400 dark:text-gray-500 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" />
          </div>

          {/* Meta information */}
          <div className="flex flex-wrap items-center gap-3 text-xs">
            {/* Edit count */}
            <div className="flex items-center gap-1 text-primary-600 dark:text-primary-400 font-semibold">
              <Edit3 className="w-3.5 h-3.5" />
              <span>{document.editCount} edits</span>
            </div>

            {/* Language badge */}
            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getLanguageBadgeColor(document.language)}`}>
              {getLanguageName(document.language)}
            </span>

            {/* Last edit time */}
            <div className="flex items-center gap-1 text-gray-500 dark:text-gray-400">
              <Clock className="w-3.5 h-3.5" />
              <span>{formatRelativeTime(document.lastEditTime)}</span>
            </div>
          </div>
        </div>
      </div>
    </a>
  );
};

export const PopularDocuments = () => {
  const { data, isLoading } = usePopularDocuments();
  const [activeTab, setActiveTab] = useState<TabType>('global');

  // Get data for current tab
  const getCurrentDocuments = (): PopularDocument[] => {
    if (!data) return [];

    switch (activeTab) {
      case 'global': return data.globalTop10;
      case 'english': return data.englishTop10;
      case 'korean': return data.koreanTop10;
      default: return [];
    }
  };

  const documents = getCurrentDocuments();

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <TrendingUp className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Popular Documents Top 10</h2>
        </div>
        {data && (
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Last {data.windowMinutes} min
          </span>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 p-1 bg-gray-100 dark:bg-gray-700 rounded-lg">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-md text-sm font-medium transition-all duration-200 ${
              activeTab === tab.id
                ? 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <span className="text-base">{tab.emoji}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="h-[500px] flex items-center justify-center">
          <LoadingSpinner size={32} />
        </div>
      ) : documents.length > 0 ? (
        <>
          <div className="space-y-3 max-h-[600px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-gray-300 dark:scrollbar-thumb-gray-600 scrollbar-track-transparent">
            {documents.map((doc, index) => (
              <DocumentCard key={index} document={doc} rank={index + 1} />
            ))}
          </div>

          {/* Last update time */}
          {data && (
            <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
                Last updated: {formatUTCTime(data.timestamp)}
              </p>
            </div>
          )}
        </>
      ) : (
        <div className="h-[500px] flex items-center justify-center text-gray-500 dark:text-gray-400">
          <p className="text-sm">No data available</p>
        </div>
      )}
    </div>
  );
};
