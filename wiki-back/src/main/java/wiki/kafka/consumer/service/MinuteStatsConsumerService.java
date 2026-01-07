package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.MinuteStatsMessage;
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
public class MinuteStatsConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;
    
    /**
     * Consumes per-minute edit statistics
     */
    @KafkaListener(
            topics = "wiki.stats.minute",
            groupId = "minute-stats-group"
    )
    public void consumeMinuteStats(@Payload String statsJson,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received minute stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to MinuteStatsMessage
            MinuteStatsMessage statsMessage = objectMapper.readValue(statsJson, MinuteStatsMessage.class);

            // Save to Redis
            redisStatsService.saveMinuteStats(statsMessage);

            // Commit offset
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process minute stats from offset {}: {}", offset, e.getMessage());
            acknowledgment.acknowledge();
        }
    }
    
}