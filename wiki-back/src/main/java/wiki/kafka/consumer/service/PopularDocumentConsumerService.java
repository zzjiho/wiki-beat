package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.PopularDocumentStatsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularDocumentConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;

    /**
     * Consumes popular document statistics
     */
    @KafkaListener(
            topics = "wiki.stats.popular",
            groupId = "popular-stats-group"
    )
    public void consumePopularDocumentStats(@Payload String statsJson,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                            @Header(KafkaHeaders.OFFSET) long offset,
                                            Acknowledgment acknowledgment) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Received popular document stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to PopularDocumentStatsMessage
            PopularDocumentStatsMessage statsMessage = objectMapper.readValue(
                    statsJson, PopularDocumentStatsMessage.class);

            // Save to Redis
            redisStatsService.savePopularDocumentStats(statsMessage);

            // Commit offset
            acknowledgment.acknowledge();

            log.info("🔥 [Popular Document Consumer] Saved popular document stats: global={}, en={}, ko={}",
                    statsMessage.getGlobalTop10().size(),
                    statsMessage.getEnglishTop10().size(),
                    statsMessage.getKoreanTop10().size());

        } catch (Exception e) {
            log.error("Failed to process popular document stats from offset {}: {}", offset, e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
