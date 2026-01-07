package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.SizeStatsMessage;
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
public class SizeStatsConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;

    /**
     * Consumes edit size statistics
     */
    @KafkaListener(
            topics = "wiki.stats.size",
            groupId = "size-stats-group"
    )
    public void consumeSizeStats(@Payload String statsJson,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                Acknowledgment acknowledgment) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Received size stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to SizeStatsMessage
            SizeStatsMessage statsMessage = objectMapper.readValue(statsJson, SizeStatsMessage.class);

            // Save to Redis
            redisStatsService.saveSizeStats(statsMessage);

            // Commit offset
            acknowledgment.acknowledge();

            log.info("📊 [Size Stats Consumer] Saved size stats: avg={} bytes, total edits={}",
                    String.format("%.1f", statsMessage.getAverageSize()),
                    statsMessage.getTotalEditCount());

        } catch (Exception e) {
            log.error("Failed to process size stats from offset {}: {}", offset, e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
