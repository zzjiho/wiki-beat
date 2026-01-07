package wiki.config;

import wiki.domain.edit.service.WikiStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StreamAutoStartConfig {

    private final WikiStreamService wikiStreamService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application started. Starting wiki stream automatically..!");
        wikiStreamService.startStreaming();
    }
}
