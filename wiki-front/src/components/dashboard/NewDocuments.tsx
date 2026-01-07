import { FileText, ExternalLink, User, Bot, Clock, Sparkles, Users, TrendingUp } from 'lucide-react';
import { useNewDocuments } from '../../hooks/queries';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { formatRelativeTime, formatUTCTime } from '../../utils/formatters';
import { getLanguageName, getLanguageBadgeColor } from '../../utils/languages';
import type { NewDocument } from '../../types';

interface DocumentItemProps {
  document: NewDocument;
}

const DocumentItem = ({ document }: DocumentItemProps) => {
  return (
    <a
      href={document.url}
      target="_blank"
      rel="noopener noreferrer"
      className="group block p-3 rounded-lg bg-gray-50 dark:bg-gray-700/50 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 hover:shadow-md"
    >
      <div className="flex items-start gap-3">
        {/* Icon */}
        <div className="flex-shrink-0 mt-0.5">
          <Sparkles className="w-4 h-4 text-primary-500 dark:text-primary-400" />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          {/* Title */}
          <div className="flex items-start justify-between gap-2 mb-2">
            <h4 className="text-sm font-medium text-gray-900 dark:text-white line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {document.title}
            </h4>
            <ExternalLink className="w-3.5 h-3.5 text-gray-400 dark:text-gray-500 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" />
          </div>

          {/* Meta information */}
          <div className="flex flex-wrap items-center gap-2 text-xs">
            {/* Creator */}
            <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
              {document.isBot ? (
                <Bot className="w-3 h-3" />
              ) : (
                <User className="w-3 h-3" />
              )}
              <span className="truncate max-w-[100px]">{document.creator}</span>
            </div>

            {/* Language badge */}
            <span className={`px-1.5 py-0.5 rounded text-xs font-medium ${getLanguageBadgeColor(document.language)}`}>
              {getLanguageName(document.language)}
            </span>

            {/* Creation time */}
            <div className="flex items-center gap-1 text-gray-500 dark:text-gray-400">
              <Clock className="w-3 h-3" />
              <span>{formatRelativeTime(document.createdTime)}</span>
            </div>
          </div>
        </div>
      </div>
    </a>
  );
};

export const NewDocuments = () => {
  const { data, isLoading } = useNewDocuments();

  // Extract top 5 languages
  const getTopLanguages = (): Array<{ language: string; count: number }> => {
    if (!data?.byLanguage) return [];

    return Object.entries(data.byLanguage)
      .map(([language, count]) => ({ language, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  };

  const topLanguages = getTopLanguages();

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <FileText className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">New Documents</h2>
        </div>
        {data && (
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Last {data.windowMinutes} min
          </span>
        )}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="h-[600px] flex items-center justify-center">
          <LoadingSpinner size={32} />
        </div>
      ) : data ? (
        <>
          {/* Statistics Summary Cards */}
          <div className="grid grid-cols-3 gap-3 mb-6">
            {/* Total New Documents */}
            <div className="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-lg p-4 border border-blue-200 dark:border-blue-800">
              <div className="flex items-center gap-2 mb-1">
                <TrendingUp className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                <span className="text-xs font-medium text-blue-600 dark:text-blue-400">Total Docs</span>
              </div>
              <p className="text-2xl font-bold text-blue-700 dark:text-blue-300">
                {data.totalNewDocuments.toLocaleString()}
              </p>
            </div>

            {/* User Created */}
            <div className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-lg p-4 border border-green-200 dark:border-green-800">
              <div className="flex items-center gap-2 mb-1">
                <Users className="w-4 h-4 text-green-600 dark:text-green-400" />
                <span className="text-xs font-medium text-green-600 dark:text-green-400">Users</span>
              </div>
              <p className="text-2xl font-bold text-green-700 dark:text-green-300">
                {data.userCreatedCount.toLocaleString()}
              </p>
            </div>

            {/* Bot Created */}
            <div className="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-lg p-4 border border-purple-200 dark:border-purple-800">
              <div className="flex items-center gap-2 mb-1">
                <Bot className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                <span className="text-xs font-medium text-purple-600 dark:text-purple-400">Bots</span>
              </div>
              <p className="text-2xl font-bold text-purple-700 dark:text-purple-300">
                {data.botCreatedCount.toLocaleString()}
              </p>
            </div>
          </div>

          {/* Top Languages Statistics */}
          {topLanguages.length > 0 && (
            <div className="mb-6">
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Top Languages</h3>
              <div className="space-y-2">
                {topLanguages.map((item, index) => (
                  <div key={item.language} className="flex items-center gap-3">
                    <div className="w-6 text-center text-xs font-bold text-gray-500 dark:text-gray-400">
                      {index + 1}
                    </div>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getLanguageBadgeColor(item.language)}`}>
                      {getLanguageName(item.language)}
                    </span>
                    <div className="flex-1 bg-gray-200 dark:bg-gray-700 rounded-full h-2 overflow-hidden">
                      <div
                        className="bg-primary-500 dark:bg-primary-400 h-full transition-all duration-300"
                        style={{ width: `${(item.count / topLanguages[0].count) * 100}%` }}
                      />
                    </div>
                    <span className="text-sm font-semibold text-gray-700 dark:text-gray-300 w-8 text-right">
                      {item.count}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Recently Created Documents List */}
          <div>
            <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Recently Created</h3>
            <div className="space-y-2 max-h-[400px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-gray-300 dark:scrollbar-thumb-gray-600 scrollbar-track-transparent">
              {data.recentDocuments.map((doc, index) => (
                <DocumentItem key={index} document={doc} />
              ))}
            </div>
          </div>

          {/* Last Update Time */}
          <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
            <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
              Last updated: {formatUTCTime(data.timestamp)}
            </p>
          </div>
        </>
      ) : (
        <div className="h-[600px] flex items-center justify-center text-gray-500 dark:text-gray-400">
          <p className="text-sm">No data available!</p>
        </div>
      )}
    </div>
  );
};
