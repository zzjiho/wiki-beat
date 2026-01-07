import { LanguageDistributionChart } from '../components/dashboard/LanguageDistributionChart';
import { RankingPanel } from '../components/dashboard/RankingPanel';
import { EditsPerMinuteChart } from '../components/dashboard/EditsPerMinuteChart';
import { BotRatioChart } from '../components/dashboard/BotRatioChart';
import { StatsOverview } from '../components/dashboard/StatsOverview';
import { PopularDocuments } from '../components/dashboard/PopularDocuments';
import { NewDocuments } from '../components/dashboard/NewDocuments';

export const Dashboard = () => {
  return (
    <div className="space-y-6">
      {/* Top: Stats overview cards */}
      <StatsOverview />

      {/* Upper section: Edits per minute & Bot ratio charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <EditsPerMinuteChart />
        <BotRatioChart />
      </div>

      {/* Language distribution chart - Full width */}
        {/*downtime test*/}
      <LanguageDistributionChart />

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* New documents - Left column */}
        <div className="lg:col-span-1">
          <NewDocuments />
        </div>

        {/* Popular documents - Center column */}
        <div className="lg:col-span-1">
          <PopularDocuments />
        </div>

        {/* Right sidebar */}
        <div className="space-y-6">
          {/* Ranking panel */}
          <RankingPanel />
        </div>
      </div>
    </div>
  );
};
