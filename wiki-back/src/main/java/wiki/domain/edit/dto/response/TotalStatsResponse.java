package wiki.domain.edit.dto.response;

import java.util.Map;

/**
 * Total edit statistics API response DTO
 */
public record TotalStatsResponse(
        long currentMinuteEdits,
        double avgEditsPerMinute,
        double avgBotRatio,
        Map<String, Object> perMinuteLanguageDistribution,
        String timeWindow,
        String description
) {
    public static TotalStatsResponse of(
            long currentMinuteEdits,
            double avgEditsPerMinute,
            double avgBotRatio,
            Map<String, Object> perMinuteLanguages,
            int minutes
    ) {
        return new TotalStatsResponse(
                currentMinuteEdits,
                Math.round(avgEditsPerMinute * 100.0) / 100.0,
                Math.round(avgBotRatio * 100.0) / 100.0,
                perMinuteLanguages,
                String.format("Last %d minutes", minutes),
                "Average per-minute statistics (not cumulative)"
        );
    }
}
