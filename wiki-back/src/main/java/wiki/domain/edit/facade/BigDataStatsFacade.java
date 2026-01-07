package wiki.domain.edit.facade;

import wiki.domain.edit.dto.response.*;
import wiki.domain.edit.service.RedisStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * BigData Statistics Facade
 * Mediates between Controller and internal services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BigDataStatsFacade {

    private final RedisStatsService redisStatsService;

    /**
     * Retrieves real-time dashboard data
     */
    public DashboardResponse getDashboard() {
        Map<String, Object> dashboard = redisStatsService.getDashboardStats();
        return DashboardResponse.from(dashboard);
    }

    /**
     * Retrieves total edit statistics
     */
    public TotalStatsResponse getTotalStats(int minutes) {
        long currentMinuteEdits = redisStatsService.getCurrentMinuteEdits();
        double avgEditsPerMinute = redisStatsService.getAverageEditsPerMinute(minutes);
        Map<String, Object> perMinuteLanguages = redisStatsService.getPerMinuteLanguageDistribution(minutes);
        double avgBotRatio = redisStatsService.getAverageBotRatio();

        return TotalStatsResponse.of(
                currentMinuteEdits,
                avgEditsPerMinute,
                avgBotRatio,
                perMinuteLanguages,
                minutes
        );
    }

    /**
     * Retrieves top 10 popular documents
     */
    public PopularDocumentsResponse getPopularDocuments() {
        return PopularDocumentsResponse.from(
                redisStatsService.getPopularDocumentStats()
        );
    }

    /**
     * Retrieves new document creation statistics
     */
    public NewDocumentsResponse getNewDocuments() {
        return NewDocumentsResponse.from(
                redisStatsService.getNewDocumentStats()
        );
    }

    /**
     * Retrieves vandalism and revert statistics
     */
    public VandalismStatsResponse getVandalismStats() {
        return VandalismStatsResponse.from(
                redisStatsService.getVandalismStats()
        );
    }
}
