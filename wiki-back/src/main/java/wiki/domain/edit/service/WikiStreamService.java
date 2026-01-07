package wiki.domain.edit.service;

import wiki.exception.WikiStreamException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikiStreamService {

    private final WikiStreamConnector streamConnector;
    private final WikiEditProcessor editProcessor;

    @Qualifier("reconnectScheduler")
    private final ThreadPoolTaskScheduler reconnectScheduler;

    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private final AtomicBoolean shouldContinue = new AtomicBoolean(false);
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private volatile ScheduledFuture<?> reconnectTask;

    /**
     * Starts Wikimedia streaming
     * Uses @Async for separate thread execution
     * Uses AtomicBoolean.compareAndSet to prevent race conditions
     */
    @Async("wikiStreamExecutor")
    public void startStreaming() {
        if (!isStreaming.compareAndSet(false, true)) {
            log.warn("Streaming is already running");
            return;
        }

        shouldContinue.set(true);
        log.info("Starting Wikimedia streaming...");

        try {
            streamConnector.connectAndProcess(this::processLine, shouldContinue);
        } catch (Exception e) {
            log.error("Error during streaming", e);
            handleStreamError();
        }
    }

    /**
     * Handles stream errors and schedules reconnection
     */
    private void handleStreamError() {
        isStreaming.set(false);
        shouldContinue.set(false);
        scheduleReconnect();
    }
    
    /**
     * Stops Wikimedia streaming
     */
    public void stopStreaming() {
        if (!isStreaming.compareAndSet(true, false)) {
            log.warn("Streaming is not running");
            return;
        }

        shouldContinue.set(false);

        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(false);
            reconnectTask = null;
            log.info("Reconnect task cancelled");
        }

        log.info("Wikimedia streaming stopped. Total processed: {}, Errors: {}",
                totalProcessed.get(), totalErrors.get());
    }
    
    /**
     * Returns streaming status
     */
    public boolean isStreaming() {
        return isStreaming.get();
    }
    
    /**
     * Returns total processed edits
     */
    public long getTotalProcessed() {
        return totalProcessed.get();
    }
    
    /**
     * Returns total errors
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }
    
    /**
     * Processes a single line from the stream
     */
    private void processLine(String line) {
        editProcessor.processLine(line, totalProcessed, totalErrors);
    }
    
    /**
     * Schedules reconnection after 5 seconds
     */
    private void scheduleReconnect() {
        reconnectTask = reconnectScheduler.schedule(
            this::attemptReconnect,
            Instant.now().plusSeconds(5)
        );

        log.info("Reconnect scheduled in 5 seconds");
    }

    /**
     * Attempts to reconnect to the stream
     */
    private void attemptReconnect() {
        reconnectTask = null;

        if (!isStreaming.get()) {
            log.info("Attempting to reconnect to Wikimedia stream...");
            try {
                startStreaming();
            } catch (Exception e) {
                log.error("Reconnect failed: {}", e.getMessage());
                scheduleReconnect();
            }
        }
    }
}