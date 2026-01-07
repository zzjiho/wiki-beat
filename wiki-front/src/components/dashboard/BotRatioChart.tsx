import { useMemo } from 'react';
import { useStats } from '../../hooks/queries';
import { Bot, Users, Zap } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import type { TooltipProps, BotRatioData } from '../../types/charts';

/**
 * Custom Tooltip Component (type-safe)
 */
const CustomTooltip = ({ active, payload }: TooltipProps<BotRatioData>) => {
  if (active && payload && payload.length) {
    const data = payload[0];
    return (
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-3">
        <p className="text-sm font-semibold text-gray-900 dark:text-white mb-1">
          {data.name}
        </p>
        <p className="text-xs text-gray-600 dark:text-gray-400">
          Ratio: <span className="font-semibold text-gray-900 dark:text-white">{data.value.toFixed(1)}%</span>
        </p>
      </div>
    );
  }
  return null;
};

export const BotRatioChart = () => {
  const { data: stats, isLoading } = useStats();

  const botRatio = stats?.averageBotRatio || 0;
  const humanRatio = 100 - botRatio;

  // Memoize chart data with useMemo (prevent flickering)
  const chartData = useMemo(() => [
    { name: 'Human', value: humanRatio, color: '#10b981' },
    { name: 'Bot', value: botRatio, color: '#8b5cf6' },
  ], [humanRatio, botRatio]);

  const COLORS = ['#10b981', '#8b5cf6'];

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
          <Bot className="w-6 h-6 text-purple-600 dark:text-purple-400" />
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Editor Type Ratio
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Human vs Bot Edits
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="h-[300px] flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
        </div>
      ) : (
        <>
          {/* Pie Chart */}
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={90}
                paddingAngle={5}
                dataKey="value"
                animationDuration={800}
                animationEasing="ease-in-out"
                isAnimationActive={true}
              >
                {chartData.map((_entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index]} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>

          {/* Detailed Statistics */}
          <div className="grid grid-cols-2 gap-4 mt-6">
            {/* Human Edits */}
            <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
              <div className="flex items-center gap-2 mb-2">
                <Users className="w-5 h-5 text-green-600 dark:text-green-400" />
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Human
                </span>
              </div>
              <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                {humanRatio.toFixed(1)}%
              </div>
              <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Of total edits
              </div>
            </div>

            {/* Bot Edits */}
            <div className="bg-purple-50 dark:bg-purple-900/20 rounded-lg p-4">
              <div className="flex items-center gap-2 mb-2">
                <Zap className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Bot
                </span>
              </div>
              <div className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                {botRatio.toFixed(1)}%
              </div>
              <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Automated edits
              </div>
            </div>
          </div>

          {/* Insight */}
          <div className="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <p className="text-xs text-blue-800 dark:text-blue-300">
              💡 {humanRatio > botRatio
                ? 'Most edits are made by humans.'
                : 'Automated bot edits are highly active.'}
            </p>
          </div>
        </>
      )}
    </div>
  );
};
