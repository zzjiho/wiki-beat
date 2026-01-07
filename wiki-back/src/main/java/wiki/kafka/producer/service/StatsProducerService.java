package wiki.kafka.producer.service;

import wiki.kafka.producer.model.MinuteStatsMessage;
import wiki.kafka.producer.model.LanguageStatsMessage;
import wiki.kafka.producer.model.TotalStatsMessage;
import wiki.kafka.producer.model.PopularDocumentStatsMessage;
import wiki.kafka.producer.model.SizeStatsMessage;
import wiki.kafka.producer.model.NewDocumentStatsMessage;
import wiki.kafka.producer.model.VandalismStatsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class StatsProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public StatsProducerService(KafkaTemplate<String, String> kafkaTemplate, 
                               @Qualifier("customObjectMapper") ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String WIKI_STATS_MINUTE_TOPIC = "wiki.stats.minute";
    private static final String WIKI_STATS_LANGUAGE_TOPIC = "wiki.stats.language";
    private static final String WIKI_STATS_TOTAL_TOPIC = "wiki.stats.total";
    private static final String WIKI_STATS_POPULAR_TOPIC = "wiki.stats.popular";
    private static final String WIKI_STATS_SIZE_TOPIC = "wiki.stats.size";
    private static final String WIKI_STATS_NEWDOCUMENT_TOPIC = "wiki.stats.newdocument";
    private static final String WIKI_STATS_VANDALISM_TOPIC = "wiki.stats.vandalism";

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");

    /**
     * Sends per-minute edit statistics
     */
    public CompletableFuture<Void> sendMinuteStats(MinuteStatsMessage statsMessage) {
        try {
            String key = "minute:" + statsMessage.getMinute().format(MINUTE_FORMATTER);
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(
                    WIKI_STATS_MINUTE_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send minute stats: {}", ex.getMessage());
                        } else {
                            log.debug("Minute stats sent: {} edits for {}",
                                    statsMessage.getEditCount(),
                                    statsMessage.getMinute());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize minute stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends per-language edit statistics
     */
    public CompletableFuture<Void> sendLanguageStats(LanguageStatsMessage statsMessage) {
        try {
            String key = "language:" + statsMessage.getLanguage();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_LANGUAGE_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send language stats: {}", ex.getMessage());
                        } else {
                            log.debug("Language stats sent: {} edits for {}",
                                    statsMessage.getEditCount(),
                                    statsMessage.getLanguage());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize language stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends total edit statistics
     */
    public CompletableFuture<Void> sendTotalStats(TotalStatsMessage statsMessage) {
        try {
            String key = "total:" + System.currentTimeMillis();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_TOTAL_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send total stats: {}", ex.getMessage());
                        } else {
                            log.debug("Total stats sent: {} total edits",
                                    statsMessage.getTotalCount());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize total stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends popular document statistics
     */
    public CompletableFuture<Void> sendPopularDocumentStats(PopularDocumentStatsMessage statsMessage) {
        try {
            String key = "popular:" + System.currentTimeMillis();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_POPULAR_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send popular document stats: {}", ex.getMessage());
                        } else {
                            log.debug("Popular document stats sent: global={}, en={}, ko={}",
                                    statsMessage.getGlobalTop10().size(),
                                    statsMessage.getEnglishTop10().size(),
                                    statsMessage.getKoreanTop10().size());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize popular document stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends edit size statistics
     */
    public CompletableFuture<Void> sendSizeStats(SizeStatsMessage statsMessage) {
        try {
            String key = "size:" + System.currentTimeMillis();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_SIZE_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send size stats: {}", ex.getMessage());
                        } else {
                            log.debug("Size stats sent: avg={} bytes, total edits={}",
                                    statsMessage.getAverageSize(),
                                    statsMessage.getTotalEditCount());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize size stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends new document statistics
     */
    public CompletableFuture<Void> sendNewDocumentStats(NewDocumentStatsMessage statsMessage) {
        try {
            String key = "newdoc:" + System.currentTimeMillis();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_NEWDOCUMENT_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send new document stats: {}", ex.getMessage());
                        } else {
                            log.debug("New document stats sent: {} new documents",
                                    statsMessage.getTotalNewDocuments());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize new document stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends vandalism and revert statistics
     */
    public CompletableFuture<Void> sendVandalismStats(VandalismStatsMessage statsMessage) {
        try {
            String key = "vandalism:" + System.currentTimeMillis();
            String value = objectMapper.writeValueAsString(statsMessage);

            return kafkaTemplate.send(WIKI_STATS_VANDALISM_TOPIC, key, value)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send vandalism stats: {}", ex.getMessage());
                        } else {
                            log.debug("Vandalism stats sent: {} reverts detected",
                                    statsMessage.getTotalReverts());
                        }
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to serialize vandalism stats", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}