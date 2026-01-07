import type { LucideProps } from 'lucide-react';
import { type ComponentType } from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: ComponentType<LucideProps>;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  suffix?: string;
  loading?: boolean;
}

export const StatCard = ({ title, value, icon: Icon, trend, suffix, loading }: StatCardProps) => {
  return (
    <div className="relative overflow-hidden rounded-xl bg-white dark:bg-gray-800 p-6 shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-shadow duration-300 animate-fade-in">
      {/* Background gradient */}
      <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-primary-500/10 to-transparent rounded-full blur-2xl" />

      <div className="relative">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <p className="text-sm font-medium text-gray-600 dark:text-gray-400">{title}</p>
          <div className="p-2 rounded-lg bg-primary-100 dark:bg-primary-900/30">
            <Icon className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          </div>
        </div>

        {/* Value */}
        {loading ? (
          <div className="h-9 bg-gray-200 dark:bg-gray-700 rounded animate-pulse w-24" />
        ) : (
          <div className="flex items-baseline gap-2">
            <h3 className="text-3xl font-bold text-gray-900 dark:text-white">
              {value}
            </h3>
            {suffix && (
              <span className="text-sm text-gray-500 dark:text-gray-400">{suffix}</span>
            )}
          </div>
        )}

        {/* Trend */}
        {trend && !loading && (
          <div className="mt-2 flex items-center gap-1">
            <span className={`text-xs font-medium ${trend.isPositive ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}`}>
              {trend.isPositive ? '↑' : '↓'} {Math.abs(trend.value)}%
            </span>
            <span className="text-xs text-gray-500 dark:text-gray-400">vs previous</span>
          </div>
        )}
      </div>
    </div>
  );
};
