package wiki.kafka.consumer.service;

import wiki.domain.edit.service.RedisStatsService;
import wiki.kafka.producer.model.NewDocumentStatsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewDocumentConsumerService {

    private final ObjectMapper objectMapper;
    private final RedisStatsService redisStatsService;

    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    /**
     * Consumes new document statistics
     */
    @KafkaListener(
            topics = "wiki.stats.newdocument",
            groupId = "newdocument-stats-group"
    )
    public void consumeNewDocumentStats(@Payload String statsJson,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Received new document stats from topic: {}, partition: {}, offset: {}",
                        topic, partition, offset);
            }

            // Convert JSON to NewDocumentStatsMessage
            NewDocumentStatsMessage statsMessage = objectMapper.readValue(
                    statsJson, NewDocumentStatsMessage.class);

            // Save to Redis
            redisStatsService.saveNewDocumentStats(statsMessage);

            processedCount.incrementAndGet();

            // Commit offset
            acknowledgment.acknowledge();

            log.info("📝 [New Document Consumer] Saved new document stats: {} new documents",
                    statsMessage.getTotalNewDocuments());

        } catch (Exception e) {
            log.error("Failed to process new document stats from offset {}: {}", offset, e.getMessage());
            errorCount.incrementAndGet();
            acknowledgment.acknowledge();
        }
    }
}
