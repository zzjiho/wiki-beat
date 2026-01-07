package wiki.domain.edit.dto.response;

import wiki.kafka.producer.model.LanguageStatsMessage;

import java.util.List;
import java.util.Map;

/**
 * Dashboard API response DTO
 */
public record DashboardResponse(
        Long totalEdits,
        Double botRatio,
        Double newPageRatio,
        List<LanguageStatsMessage> topLanguages,
        Map<String, Long> minuteChart
) {
    public static DashboardResponse from(Map<String, Object> dashboardData) {
        return new DashboardResponse(
                (Long) dashboardData.get("totalEdits"),
                (Double) dashboardData.get("botRatio"),
                (Double) dashboardData.get("newPageRatio"),
                (List<LanguageStatsMessage>) dashboardData.get("topLanguages"),
                (Map<String, Long>) dashboardData.get("minuteChart")
        );
    }
}
