package wiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "wikimedia")
public class WikimediaProperties {
    
    private Stream stream = new Stream();
    
    @Data
    public static class Stream {
        private String url = "https://stream.wikimedia.org/v2/stream/recentchange";
        private Duration reconnectDelay = Duration.ofSeconds(5);
        private int maxRetries = 10;
    }
}