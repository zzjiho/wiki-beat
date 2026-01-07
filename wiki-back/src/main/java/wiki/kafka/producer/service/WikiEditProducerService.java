package wiki.kafka.producer.service;

import wiki.kafka.producer.model.EditMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikiEditProducerService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String WIKI_RAW_EDITS_TOPIC = "wiki.raw.edits";

    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    /**
     * Sends wiki edit data to Kafka asynchronously
     */
    public CompletableFuture<Void> sendEditAsync(EditMessage editMessage) {
        try {
            String key = generatePartitionKey(editMessage);
            String value = objectMapper.writeValueAsString(editMessage);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(WIKI_RAW_EDITS_TOPIC, key, value);

            return future.handle((result, ex) -> {
                if (ex != null) {
                    totalErrors.incrementAndGet();
                    log.error("Failed to send edit to Kafka: {} - {}",
                            editMessage.getTitle(), ex.getMessage());
                } else {
                    totalSent.incrementAndGet();
                }
                return null;
            });
            
        } catch (Exception e) {
            totalErrors.incrementAndGet();
            log.error("Failed to serialize edit message: {}", editMessage.getTitle(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generates partition key based on language
     * Edits of the same language are always sent to the same partition for ordering
     */
    private String generatePartitionKey(EditMessage editMessage) {
        return editMessage.getLanguage();
    }
    
    /**
     * Forces flush of pending messages
     */
    public void flush() {
        try {
            kafkaTemplate.flush();
            log.info("Kafka producer flushed successfully");
        } catch (Exception e) {
            log.error("Failed to flush Kafka producer", e);
            throw new RuntimeException("Kafka producer flush failed", e);
        }
    }
    
    /**
     * Returns total messages sent
     */
    public long getTotalSent() {
        return totalSent.get();
    }
    
    public long getTotalErrors() {
        return totalErrors.get();
    }
    
    public double getSuccessRate() {
        long total = totalSent.get() + totalErrors.get();
        if (total == 0) return 0.0;
        return (double) totalSent.get() / total * 100.0;
    }
    
    /**
     * Resets all metrics
     */
    public void resetMetrics() {
        totalSent.set(0);
        totalErrors.set(0);
        log.info("Producer metrics reset");
    }
}