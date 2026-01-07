package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.LanguageStatsMessage;
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
public class LanguageStatsConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;
    
    /**
     * Consumes per-language edit statistics
     */
    @KafkaListener(
            topics = "wiki.stats.language",
            groupId = "language-stats-group"
    )
    public void consumeLanguageStats(@Payload String statsJson,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {
        
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received language stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to LanguageStatsMessage
            LanguageStatsMessage statsMessage = objectMapper.readValue(statsJson, LanguageStatsMessage.class);

            // Save to Redis
            redisStatsService.saveLanguageStats(statsMessage);

            // Commit offset
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process language stats from offset {}: {}", offset, e.getMessage());
            acknowledgment.acknowledge();
        }
    }
    
}