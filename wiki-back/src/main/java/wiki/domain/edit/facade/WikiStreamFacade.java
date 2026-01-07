package wiki.domain.edit.facade;

import wiki.domain.edit.dto.response.StreamStartResponse;
import wiki.domain.edit.dto.response.StreamStatusResponse;
import wiki.domain.edit.dto.response.StreamStopResponse;
import wiki.domain.edit.service.WikiStreamService;
import wiki.kafka.producer.service.WikiEditProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Wiki Stream Facade
 * Mediates between Controller and internal services
 * Manages stream lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WikiStreamFacade {

    private final WikiStreamService wikiStreamService;
    private final WikiEditProducerService wikiEditProducerService;

    public StreamStartResponse startStream() {
        if (wikiStreamService.isStreaming()) {
            log.warn("Stream is already running");
            return StreamStartResponse.alreadyRunning(
                    wikiStreamService.getTotalProcessed(),
                    wikiStreamService.getTotalErrors()
            );
        }

        wikiStreamService.startStreaming();

        return StreamStartResponse.started();
    }

    public StreamStopResponse stopStream() {
        if (!wikiStreamService.isStreaming()) {
            log.warn("Stream is not running");
            return StreamStopResponse.notRunning();
        }

        wikiEditProducerService.flush();
        wikiStreamService.stopStreaming();

        return StreamStopResponse.stopped(
                wikiStreamService.getTotalProcessed(),
                wikiStreamService.getTotalErrors(),
                wikiEditProducerService.getTotalSent(),
                wikiEditProducerService.getTotalErrors()
        );
    }

    public StreamStatusResponse getStreamStatus() {
        return StreamStatusResponse.of(
                wikiStreamService.isStreaming(),
                wikiStreamService.getTotalProcessed(),
                wikiStreamService.getTotalErrors(),
                wikiEditProducerService.getTotalSent(),
                wikiEditProducerService.getTotalErrors(),
                wikiEditProducerService.getSuccessRate()
        );
    }
}
