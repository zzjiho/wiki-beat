package wiki.kafka.consumer.service;

import wiki.kafka.producer.model.EditMessage;
import wiki.kafka.producer.service.StatsProducerService;
import wiki.kafka.consumer.handler.StatsAggregator;
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
public class RawEditConsumerService {
    
    private final ObjectMapper objectMapper;
    private final StatsAggregator statsAggregator;
    private final StatsProducerService statsProducerService;
    
    // Metrics
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    /**
     * Consumes raw edit data and processes statistics aggregation
     */
    @KafkaListener(
            topics = "wiki.raw.edits",
            groupId = "wiki-stats-group"
    )
    public void consumeRawEdit(@Payload String editJson,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset,
                              Acknowledgment acknowledgment) {
        
        try {
            EditMessage editMessage = objectMapper.readValue(editJson, EditMessage.class);

            // Aggregate statistics in memory
            statsAggregator.aggregateEdit(editMessage);

            // Log Korean edits only
            if ("ko".equals(editMessage.getLanguage())) {
                log.info("🇰🇷 [Korean Edit] {} | user: {} | size: {} | bot: {} | new: {} | time: {}",
                        editMessage.getTitle(),
                        editMessage.getUser(),
                        editMessage.getSizeDiff(),
                        editMessage.isBotEdit(),
                        editMessage.isNewPage(),
                        editMessage.getTimestamp());
            }

            // Increment processed count
            processedCount.incrementAndGet();

            // Commit offset after processing
            acknowledgment.acknowledge();

            // Log progress every 10,000 edits
            if (processedCount.get() % 10000 == 0) {
                log.info("Processed: {}, Errors: {}, Recent: {} ({})",
                        processedCount.get(),
                        errorCount.get(),
                        editMessage.getTitle(),
                        editMessage.getLanguage());
            }
            
        } catch (Exception e) {
            log.error("Failed to process edit from offset {}: {}", offset, e.getMessage());
            errorCount.incrementAndGet();
            
            // Commit offset even on error to prevent infinite retry loop
            acknowledgment.acknowledge();
        }
    }
}