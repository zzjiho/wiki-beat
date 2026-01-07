import { TrendingUp, Bot, FileEdit } from 'lucide-react';
import { StatCard } from './StatCard';
import { useStats } from '../../hooks/queries';

export const StatsOverview = () => {
  const { data: stats, isLoading } = useStats();

  // Calculate peak edits per minute (last 10 minutes only, matching the graph)
  const peakEditsPerMin = stats?.recentMinutes
    ? Math.max(...Object.entries(stats.recentMinutes)
        .map(([_, minuteStats]) => minuteStats)
        .slice(-10) // Only last 10 entries to match the graph
        .map(m => m.editCount))
    : 0;

  // Calculate per-minute averages
  const avgEditsPerMin = stats?.averageEditsPerMinute || 0;
  const avgBotEdits = avgEditsPerMin * (stats?.averageBotRatio || 0) / 100;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard
        title="Peak Edits/Min (10 min)"
        value={stats ? peakEditsPerMin.toFixed(0) : '0'}
        icon={TrendingUp}
        suffix="/min"
        loading={isLoading}
      />

      <StatCard
        title="Avg Bot Edits/Min (10 min)"
        value={stats ? avgBotEdits.toFixed(1) : '0'}
        icon={Bot}
        suffix="/min"
        loading={isLoading}
      />

      <StatCard
        title="Bot Edit Ratio (10 min)"
        value={stats ? `${stats.averageBotRatio.toFixed(1)}%` : '0%'}
        icon={Bot}
        loading={isLoading}
      />

      <StatCard
        title="New Page Ratio"
        value={stats ? `${((stats.totalNewPages / stats.totalCount) * 100).toFixed(1)}%` : '0%'}
        icon={FileEdit}
        loading={isLoading}
      />
    </div>
  );
};
