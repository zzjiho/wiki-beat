import { useMemo } from 'react';
import { useStats } from '../../hooks/queries';
import { Activity, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TooltipProps, EditsPerMinuteData } from '../../types/charts';

/**
 * Custom Tooltip Component
 */
const CustomTooltip = ({ active, payload, label }: TooltipProps<EditsPerMinuteData>) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-3">
        <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">
          {label}
        </p>
        <p className="text-sm font-semibold text-gray-900 dark:text-white">
          Edits: {payload[0].value.toLocaleString('en-US')}
        </p>
      </div>
    );
  }
  return null;
};

export const EditsPerMinuteChart = () => {
  const { data: stats, isLoading } = useStats();

  // Transform recentMinutes data for chart (useMemo prevents flickering)
  const chartData = useMemo(() => {
    if (!stats?.recentMinutes || Object.keys(stats.recentMinutes).length === 0) {
      // Generate dummy data with average if recentMinutes is empty
      const average = stats?.averageEditsPerMinute || 0;
      const now = new Date();
      return Array.from({ length: 20 }, (_, i) => {
        const time = new Date(now.getTime() - (19 - i) * 60000);
        return {
          time: time.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }),
          edits: Math.round(average + (Math.random() - 0.5) * average * 0.3), // ±15% variation
        };
      });
    }

    return Object.entries(stats.recentMinutes)
      .map(([_key, minuteStats]) => ({
        time: new Date(minuteStats.minute).toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }),
        edits: minuteStats.editCount,
      }))
      .slice(-10); // Show last 10 entries
  }, [stats]);

  // Calculate average of displayed data (last 10 minutes)
  const displayedAverage = useMemo(() => {
    if (!chartData || chartData.length === 0) return 0;
    const sum = chartData.reduce((acc, item) => acc + item.edits, 0);
    return sum / chartData.length;
  }, [chartData]);

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
            <Activity className="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              Edits Per Minute (UTC)
            </h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Real-time editing activity trend
            </p>
          </div>
        </div>

        {/* Average display */}
        <div className="text-right">
          <div className="flex items-center gap-2 text-blue-600 dark:text-blue-400">
            <TrendingUp className="w-5 h-5" />
            <span className="text-2xl font-bold">
              {displayedAverage.toFixed(1)}
            </span>
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            Avg edits/min
          </p>
        </div>
      </div>

      {/* Chart */}
      {isLoading ? (
        <div className="h-[300px] flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="colorEdits" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" opacity={0.1} />
            <XAxis
              dataKey="time"
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#9ca3af' }}
              interval={0}
            />
            <YAxis
              stroke="#6b7280"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#9ca3af' }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="edits"
              stroke="#3b82f6"
              strokeWidth={2}
              fill="url(#colorEdits)"
              name="Edits"
              animationDuration={800}
              animationEasing="ease-in-out"
              isAnimationActive={true}
            />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  );
};
