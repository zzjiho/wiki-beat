package wiki.domain.edit.service;

import wiki.kafka.producer.model.MinuteStatsMessage;
import wiki.kafka.producer.model.LanguageStatsMessage;
import wiki.kafka.producer.model.TotalStatsMessage;
import wiki.kafka.producer.model.PopularDocumentStatsMessage;
import wiki.kafka.producer.model.NewDocumentStatsMessage;
import wiki.kafka.producer.model.VandalismStatsMessage;
import wiki.kafka.producer.model.SizeStatsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStatsService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");

    private static final String MINUTE_STATS_PREFIX = "minute:";
    private static final String LANGUAGE_STATS_PREFIX = "language:";
    private static final String TOTAL_STATS_KEY = "total:stats";
    private static final String POPULAR_DOCUMENTS_KEY = "popular:documents";
    private static final String NEWDOCUMENT_STATS_KEY = "newdocument:stats";
    private static final String VANDALISM_STATS_KEY = "vandalism:stats";
    private static final String SIZE_STATS_KEY = "size:stats";

    private static final long MINUTE_STATS_TTL_HOURS = 1;
    private static final long AGGREGATE_STATS_TTL_HOURS = 24;
    private static final long REALTIME_STATS_TTL_HOURS = 24;

    /**
     * Saves per-minute edit statistics
     */
    public void saveMinuteStats(MinuteStatsMessage stats) {
        String key = MINUTE_STATS_PREFIX + stats.getMinute().format(MINUTE_FORMATTER);
        redisTemplate.opsForValue().set(key, stats, MINUTE_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved minute stats to Redis: {} edits at {}", stats.getEditCount(), stats.getMinute());
    }

    /**
     * Saves per-language edit statistics
     */
    public void saveLanguageStats(LanguageStatsMessage stats) {
        String key = LANGUAGE_STATS_PREFIX + stats.getLanguage();
        redisTemplate.opsForValue().set(key, stats, AGGREGATE_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved language stats to Redis: {} - {} edits", stats.getLanguage(), stats.getEditCount());
    }

    /**
     * Saves total edit statistics
     */
    public void saveTotalStats(TotalStatsMessage stats) {
        redisTemplate.opsForValue().set(TOTAL_STATS_KEY, stats, AGGREGATE_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved total stats to Redis: {} total edits", stats.getTotalCount());
    }

    /**
     * Retrieves recent per-minute edit statistics (last 60 minutes)
     */
    public Map<String, MinuteStatsMessage> getRecentMinuteStats() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, MinuteStatsMessage> result = new LinkedHashMap<>();

        for (int i = 59; i >= 0; i--) {
            LocalDateTime minute = now.minusMinutes(i).withSecond(0).withNano(0);
            String key = MINUTE_STATS_PREFIX + minute.format(MINUTE_FORMATTER);

            MinuteStatsMessage stats = (MinuteStatsMessage) redisTemplate.opsForValue().get(key);
            if (stats != null) {
                result.put(minute.format(MINUTE_FORMATTER), stats);
            }
        }

        return result;
    }

    /**
     * Retrieves total statistics
     */
    public TotalStatsMessage getTotalStats() {
        return (TotalStatsMessage) redisTemplate.opsForValue().get(TOTAL_STATS_KEY);
    }

    /**
     * Retrieves aggregated statistics for real-time dashboard
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        TotalStatsMessage totalStats = getTotalStats();
        if (totalStats != null) {
            dashboard.put("totalEdits", totalStats.getTotalCount());
            dashboard.put("botRatio", totalStats.getTotalBotRatio());
            dashboard.put("newPageRatio", totalStats.getNewPageRatio());
        } else {
            dashboard.put("totalEdits", 0L);
            dashboard.put("botRatio", 0.0);
            dashboard.put("newPageRatio", 0.0);
        }

        dashboard.put("topLanguages", getTopLanguagesByEdits(10));

        Map<String, MinuteStatsMessage> recentMinutes = getRecentMinuteStats();
        Map<String, Long> minuteChart = new LinkedHashMap<>();
        recentMinutes.entrySet().stream()
                .skip(Math.max(0, recentMinutes.size() - 30))
                .forEach(entry -> minuteChart.put(entry.getKey(), entry.getValue().getEditCount()));
        dashboard.put("minuteChart", minuteChart);

        return dashboard;
    }

    /**
     * Calculates average edits per minute for the last N minutes
     */
    public double getAverageEditsPerMinute(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        Map<String, MinuteStatsMessage> recentMinutes = getRecentMinuteStats();

        java.util.List<MinuteStatsMessage> filteredStats = recentMinutes.values().stream()
            .filter(stats -> stats.getMinute().isAfter(cutoff))
            .collect(java.util.stream.Collectors.toList());

        if (filteredStats.isEmpty()) {
            return 0.0;
        }

        long totalEdits = filteredStats.stream()
            .mapToLong(MinuteStatsMessage::getEditCount)
            .sum();

        return (double) totalEdits / filteredStats.size();
    }

    /**
     * Retrieves current minute's real-time edit count
     */
    public long getCurrentMinuteEdits() {
        LocalDateTime currentMinute = LocalDateTime.now()
            .withSecond(0)
            .withNano(0);
        String key = MINUTE_STATS_PREFIX + currentMinute.format(MINUTE_FORMATTER);

        MinuteStatsMessage stats = (MinuteStatsMessage) redisTemplate.opsForValue().get(key);
        return stats != null ? stats.getEditCount() : 0L;
    }

    /**
     * Calculates average bot edit ratio for the last hour
     */
    public double getAverageBotRatio() {
        Map<String, MinuteStatsMessage> recentMinutes = getRecentMinuteStats();

        if (recentMinutes.isEmpty()) {
            return 0.0;
        }

        double totalRatio = recentMinutes.values().stream()
            .mapToDouble(MinuteStatsMessage::getBotEditRatio)
            .sum();

        return totalRatio / recentMinutes.size();
    }

    /**
     * Retrieves all language statistics
     */
    public Map<String, LanguageStatsMessage> getAllLanguageStats() {
        Map<String, LanguageStatsMessage> result = new LinkedHashMap<>();
        Set<String> keys = redisTemplate.keys(LANGUAGE_STATS_PREFIX + "*");

        if (keys != null) {
            for (String key : keys) {
                LanguageStatsMessage stats = (LanguageStatsMessage) redisTemplate.opsForValue().get(key);
                if (stats != null) {
                    String language = key.replace(LANGUAGE_STATS_PREFIX, "");
                    result.put(language, stats);
                }
            }
        }

        return result;
    }

    /**
     * Retrieves top languages by edit count
     */
    public java.util.List<LanguageStatsMessage> getTopLanguagesByEdits(int limit) {
        Map<String, LanguageStatsMessage> allStats = getAllLanguageStats();

        return allStats.values().stream()
            .sorted((a, b) -> Long.compare(b.getEditCount(), a.getEditCount()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves per-minute language distribution for the last N minutes
     * @param minutes number of minutes to query
     * @return average per-minute edits and percentage per language
     */
    public Map<String, Object> getPerMinuteLanguageDistribution(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> languageTotals = new java.util.HashMap<>();
        int validMinutes = 0;

        for (int i = minutes - 1; i >= 0; i--) {
            LocalDateTime minute = now.minusMinutes(i).withSecond(0).withNano(0);
            String key = MINUTE_STATS_PREFIX + minute.format(MINUTE_FORMATTER);

            MinuteStatsMessage stats = (MinuteStatsMessage) redisTemplate.opsForValue().get(key);
            if (stats != null && stats.getLanguageDistribution() != null) {
                validMinutes++;
                stats.getLanguageDistribution().forEach((language, count) ->
                    languageTotals.merge(language, count, Long::sum)
                );
            }
        }

        if (validMinutes == 0) {
            return Map.of(
                "averagePerMinute", Map.of(),
                "distribution", Map.of(),
                "totalMinutes", 0
            );
        }

        long totalEdits = languageTotals.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> averagePerMinute = new LinkedHashMap<>();
        Map<String, Double> distribution = new LinkedHashMap<>();

        final int finalValidMinutes = validMinutes;
        languageTotals.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(20)
            .forEach(entry -> {
                String language = entry.getKey();
                Long total = entry.getValue();
                double average = (double) total / finalValidMinutes;
                double percentage = totalEdits > 0 ? (double) total / totalEdits * 100.0 : 0.0;

                averagePerMinute.put(language, Math.round(average * 100.0) / 100.0);
                distribution.put(language, Math.round(percentage * 100.0) / 100.0);
            });

        return Map.of(
            "averagePerMinute", averagePerMinute,
            "distribution", distribution,
            "totalMinutes", validMinutes
        );
    }

    /**
     * Saves popular document statistics
     */
    public void savePopularDocumentStats(PopularDocumentStatsMessage stats) {
        redisTemplate.opsForValue().set(POPULAR_DOCUMENTS_KEY, stats, REALTIME_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved popular document stats to Redis: global={}, en={}, ko={}",
                stats.getGlobalTop10().size(),
                stats.getEnglishTop10().size(),
                stats.getKoreanTop10().size());
    }

    /**
     * Retrieves popular document statistics
     */
    public PopularDocumentStatsMessage getPopularDocumentStats() {
        return (PopularDocumentStatsMessage) redisTemplate.opsForValue().get(POPULAR_DOCUMENTS_KEY);
    }

    /**
     * Saves new document statistics
     */
    public void saveNewDocumentStats(NewDocumentStatsMessage stats) {
        redisTemplate.opsForValue().set(NEWDOCUMENT_STATS_KEY, stats, REALTIME_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved new document stats to Redis: {} new documents", stats.getTotalNewDocuments());
    }

    /**
     * Retrieves new document statistics
     */
    public NewDocumentStatsMessage getNewDocumentStats() {
        return (NewDocumentStatsMessage) redisTemplate.opsForValue().get(NEWDOCUMENT_STATS_KEY);
    }

    /**
     * Saves vandalism and revert statistics
     */
    public void saveVandalismStats(VandalismStatsMessage stats) {
        redisTemplate.opsForValue().set(VANDALISM_STATS_KEY, stats, REALTIME_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved vandalism stats to Redis: {} reverts detected", stats.getTotalReverts());
    }

    /**
     * Retrieves vandalism and revert statistics
     */
    public VandalismStatsMessage getVandalismStats() {
        return (VandalismStatsMessage) redisTemplate.opsForValue().get(VANDALISM_STATS_KEY);
    }

    /**
     * Saves edit size statistics
     */
    public void saveSizeStats(SizeStatsMessage stats) {
        redisTemplate.opsForValue().set(SIZE_STATS_KEY, stats, REALTIME_STATS_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Saved size stats to Redis: avg={} bytes, total edits={}",
                stats.getAverageSize(), stats.getTotalEditCount());
    }
}
