import { useMemo } from 'react';
import { useStats } from '../../hooks/queries';
import { Globe, Languages } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { CHART_COLORS } from '../../utils/constants';
import { getLanguageName } from '../../utils/languages';
import type { TooltipProps, LanguageDistributionData } from '../../types/charts';

/**
 * Custom Tooltip Component (type-safe)
 */
const CustomTooltip = ({ active, payload }: TooltipProps<LanguageDistributionData>) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-3">
        <p className="text-sm font-semibold text-gray-900 dark:text-white mb-1">
          {data.language}
        </p>
        <p className="text-xs text-gray-600 dark:text-gray-400">
          Edits: <span className="font-semibold text-gray-900 dark:text-white">{data.count.toLocaleString('en-US')}</span> ({data.percentage}%)
        </p>
      </div>
    );
  }
  return null;
};

export const LanguageDistributionChart = () => {
  const { data: stats, isLoading } = useStats();

  /**
   * Transform chart data (using per-minute average)
   */
  const chartData = useMemo(() => {
    if (!stats?.perMinuteLanguages) return [];

    const total = Object.values(stats.perMinuteLanguages).reduce((sum, val) => sum + val, 0);

    return Object.entries(stats.perMinuteLanguages)
      .map(([code, count]) => ({
        language: getLanguageName(code, code), // Use utility function (remove duplication)
        code,
        count: Math.round(count * 10) / 10, // 1 decimal place
        percentage: ((count / total) * 100).toFixed(1),
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10); // Show top 10 only
  }, [stats]);

  // Calculate total of displayed top 10 languages
  const top10Total = useMemo(() => {
    if (!chartData || chartData.length === 0) return 0;
    return chartData.reduce((sum, item) => sum + item.count, 0);
  }, [chartData]);

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg">
            <Languages className="w-6 h-6 text-indigo-600 dark:text-indigo-400" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              Language Distribution
            </h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Per-minute average (Last 30 min)
            </p>
          </div>
        </div>

        {/* Top 10 languages total */}
        <div className="text-right">
          <div className="flex items-center gap-2 text-indigo-600 dark:text-indigo-400">
            <Globe className="w-5 h-5" />
            <span className="text-2xl font-bold">
              {top10Total.toFixed(1)}
            </span>
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            Top 10 total edits/min
          </p>
        </div>
      </div>

      {/* Chart */}
      {isLoading ? (
        <div className="h-[400px] flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
        </div>
      ) : (
        <>
          <ResponsiveContainer width="100%" height={400}>
            <BarChart
              data={chartData}
              layout="vertical"
              margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" opacity={0.1} />
              <XAxis
                type="number"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tick={{ fill: '#9ca3af' }}
              />
              <YAxis
                type="category"
                dataKey="language"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tick={{ fill: '#9ca3af' }}
                width={150}
              />
              <Tooltip content={<CustomTooltip />} />
              <Bar
                dataKey="count"
                radius={[0, 8, 8, 0]}
                animationDuration={800}
                animationEasing="ease-in-out"
              >
                {chartData.map((_entry, index) => (
                  <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>

          {/* Statistics Summary */}
          <div className="mt-6 grid grid-cols-2 md:grid-cols-3 gap-4">
            {chartData.slice(0, 3).map((item, index) => (
              <div
                key={item.code}
                className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-3"
              >
                <div className="flex items-center gap-2 mb-1">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: CHART_COLORS[index] }}
                  />
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                    {item.language}
                  </span>
                </div>
                <div className="text-xl font-bold text-gray-900 dark:text-white">
                  {item.count.toFixed(1)}
                </div>
                <div className="text-xs text-gray-500 dark:text-gray-400">
                  edits/min ({item.percentage}%)
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};
