package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.TotalStatsMessage;
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
public class TotalStatsConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;

    /**
     * Consumes total edit count statistics (order-preserving)
     */
    @KafkaListener(
            topics = "wiki.stats.total",
            groupId = "total-stats-group"
    )
    public void consumeTotalStats(@Payload String statsJson,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Received total stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to TotalStatsMessage
            TotalStatsMessage statsMessage = objectMapper.readValue(statsJson, TotalStatsMessage.class);

            // Save to Redis
            redisStatsService.saveTotalStats(statsMessage);

            // Commit offset
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process total stats from offset {}: {}", offset, e.getMessage());
            acknowledgment.acknowledge();
        }
    }

}
