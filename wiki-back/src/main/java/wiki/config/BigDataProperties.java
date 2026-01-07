package wiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "bigdata")
public class BigDataProperties {
    
    private Processing processing = new Processing();
    private Cache cache = new Cache();
    
    @Data
    public static class Processing {
        private int batchSize = 1000;
        private Duration statsInterval = Duration.ofSeconds(60);
        private Duration languageStatsInterval = Duration.ofSeconds(300);
        private Duration totalStatsInterval = Duration.ofSeconds(600);
    }
    
    @Data
    public static class Cache {
        private Duration minuteStatsTtl = Duration.ofHours(1);
        private Duration languageStatsTtl = Duration.ofHours(24);
        private Duration totalStatsTtl = Duration.ofHours(12);
    }
}