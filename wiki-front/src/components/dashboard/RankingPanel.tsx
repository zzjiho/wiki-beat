import { Shield, ExternalLink, type LucideProps } from 'lucide-react';
import { type ComponentType } from 'react';
import { useVandalismStats } from '../../hooks/queries';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { formatUTCTime } from '../../utils/formatters';
import type { RankingItem } from '../../types';

interface RankingListProps {
  title: string;
  items: RankingItem[];
  icon: ComponentType<LucideProps>;
}

const RankingList = ({ title, items, icon: Icon }: RankingListProps) => {
  return (
    <div className="mb-6 last:mb-0">
      <div className="flex items-center gap-2 mb-3">
        <Icon className="w-4 h-4 text-primary-600 dark:text-primary-400" />
        <h3 className="text-sm font-semibold text-gray-900 dark:text-white">{title}</h3>
      </div>

      <div className="space-y-2">
        {items.map((item) => (
          <div
            key={item.rank}
            className="flex items-center gap-3 p-3 rounded-lg bg-gray-50 dark:bg-gray-700/50 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          >
            {/* 순위 */}
            <div
              className={`flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold ${
                item.rank === 1
                  ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400'
                  : item.rank === 2
                  ? 'bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-300'
                  : item.rank === 3
                  ? 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400'
                  : 'bg-gray-100 dark:bg-gray-600 text-gray-600 dark:text-gray-400'
              }`}
            >
              {item.rank}
            </div>

            {/* 이름 */}
            <div className="flex-1 min-w-0">
              {item.url ? (
                <a
                  href={item.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1 text-sm font-medium text-gray-900 dark:text-white hover:text-primary-600 dark:hover:text-primary-400 truncate"
                >
                  <span className="truncate">{item.name}</span>
                  <ExternalLink className="w-3 h-3 flex-shrink-0" />
                </a>
              ) : (
                <span className="text-sm font-medium text-gray-900 dark:text-white truncate block">
                  {item.name}
                </span>
              )}
            </div>

            {/* 카운트 */}
            <div className="text-sm font-semibold text-primary-600 dark:text-primary-400">
              {item.count.toLocaleString()}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const VandalismContent = () => {
  const { data: vandalism, isLoading } = useVandalismStats();

  if (isLoading) {
    return (
      <div className="h-[400px] flex items-center justify-center">
        <LoadingSpinner size={32} />
      </div>
    );
  }

  if (!vandalism) {
    return (
      <div className="h-[400px] flex items-center justify-center text-gray-500 dark:text-gray-400">
        <p className="text-sm">No data available</p>
      </div>
    );
  }

  // Convert revert type to RankingItem format
  const revertedDocuments: RankingItem[] = vandalism.mostRevertedDocuments.slice(0, 10).map((doc, index) => ({
    rank: index + 1,
    name: `${doc.title} (${doc.language})`,
    count: doc.revertCount,
    url: doc.url,
  }));

  const revertTypeLabels: Record<string, string> = {
    revert: 'Revert',
    undo: 'Undo',
    vandalism: 'Vandalism',
    rollback: 'Rollback',
  };

  const revertTypeColors: Record<string, string> = {
    revert: 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400',
    undo: 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400',
    vandalism: 'bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400',
    rollback: 'bg-pink-100 dark:bg-pink-900/30 text-pink-700 dark:text-pink-400',
  };

  return (
    <>
      {/* Revert type summary */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-3">
          Total Reverts: {vandalism.totalReverts}
        </h3>
        <div className="flex flex-wrap gap-2">
          {Object.entries(vandalism.byType).map(([type, count]) => (
            <div
              key={type}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${
                revertTypeColors[type] || 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
              }`}
            >
              {revertTypeLabels[type] || type}: {count}
            </div>
          ))}
        </div>
      </div>

      {/* Most reverted documents */}
      <RankingList title="Most Reverted Documents" items={revertedDocuments} icon={Shield} />

      {/* Recent revert activity */}
      <div className="mt-6">
        <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-3">Recent Revert Activity</h3>
        <div className="space-y-2 max-h-[300px] overflow-y-auto">
          {vandalism.recentReverts.slice(0, 10).map((revert, index) => (
            <div
              key={index}
              className="p-3 rounded-lg bg-gray-50 dark:bg-gray-700/50 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            >
              <div className="flex items-start gap-2">
                <span
                  className={`px-2 py-0.5 rounded text-xs font-semibold flex-shrink-0 ${
                    revertTypeColors[revert.revertType] || 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
                  }`}
                >
                  {revertTypeLabels[revert.revertType] || revert.revertType}
                </span>
                <div className="flex-1 min-w-0">
                  <a
                    href={revert.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm font-medium text-gray-900 dark:text-white hover:text-primary-600 dark:hover:text-primary-400 truncate flex items-center gap-1"
                  >
                    <span className="truncate">{revert.title}</span>
                    <span className="text-xs text-gray-500 dark:text-gray-400">({revert.language})</span>
                    <ExternalLink className="w-3 h-3 flex-shrink-0" />
                  </a>
                  <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">
                    by {revert.user} • {new Date(revert.revertTime).toLocaleTimeString('en-US')}
                  </p>
                  {revert.comment && (
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 truncate" title={revert.comment}>
                      {revert.comment}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Last update time */}
      <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
        <p className="text-xs text-gray-500 dark:text-gray-400 text-center">
          Last updated: {formatUTCTime(vandalism.timestamp)}
        </p>
      </div>
    </>
  );
};

export const RankingPanel = () => {
  const { data } = useVandalismStats();

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Shield className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Edit Revert Statistics</h2>
        </div>
        {data && (
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Last {data.windowMinutes} min
          </span>
        )}
      </div>

      {/* Content */}
      <VandalismContent />
    </div>
  );
};
