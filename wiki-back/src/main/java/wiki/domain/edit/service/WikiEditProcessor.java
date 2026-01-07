package wiki.domain.edit.service;

import wiki.kafka.producer.model.EditMessage;
import wiki.kafka.producer.service.WikiEditProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Wikimedia edit data processor
 * Handles data parsing, validation, Kafka transmission, and metrics update
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WikiEditProcessor {

    private final WikiDataParser dataParser;
    private final WikiEditProducerService wikiEditProducerService;

    /**
     * Processes a stream line data
     *
     * @param line line data to process
     * @param totalProcessed total processed count
     * @param totalErrors total error count
     * @return processing success status
     */
    public boolean processLine(String line, AtomicLong totalProcessed, AtomicLong totalErrors) {
        try {
            EditMessage editMessage = parseAndValidate(line);
            if (editMessage == null) {
                return false;
            }

            sendToKafka(editMessage);
            updateMetrics(editMessage, totalProcessed, totalErrors);
            return true;

        } catch (Exception e) {
            handleProcessingError(e, totalErrors);
            return false;
        }
    }

    /**
     * Validates and converts line data to EditMessage
     */
    private EditMessage parseAndValidate(String line) {
        if (!dataParser.isValidEventData(line)) {
            return null;
        }

        return dataParser.parseToEditMessage(line);
    }

    /**
     * Sends EditMessage to Kafka
     */
    private void sendToKafka(EditMessage editMessage) {
        wikiEditProducerService.sendEditAsync(editMessage);
    }

    /**
     * Updates metrics using AtomicLong for thread safety
     */
    private void updateMetrics(EditMessage editMessage, AtomicLong totalProcessed, AtomicLong totalErrors) {
        totalProcessed.incrementAndGet();
    }

    /**
     * Handles processing errors
     */
    private void handleProcessingError(Exception e, AtomicLong totalErrors) {
        totalErrors.incrementAndGet();
        log.error("Error processing line: {}", e.getMessage());
    }
}
