package wiki.kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;
    
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic wikiRawEditsTopic() {
        return TopicBuilder.name("wiki.raw.edits")
                .partitions(16)
                .replicas(1)
                .config("retention.ms", "86400000")
                .config("compression.type", "lz4")
                .build();
    }

    @Bean
    public NewTopic wikiStatsMinuteTopic() {
        return TopicBuilder.name("wiki.stats.minute")
                .partitions(24)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsLanguageTopic() {
        return TopicBuilder.name("wiki.stats.language")
                .partitions(8)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsTotalTopic() {
        return TopicBuilder.name("wiki.stats.total")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsPopularTopic() {
        return TopicBuilder.name("wiki.stats.popular")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsSizeTopic() {
        return TopicBuilder.name("wiki.stats.size")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsNewDocumentTopic() {
        return TopicBuilder.name("wiki.stats.newdocument")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic wikiStatsVandalismTopic() {
        return TopicBuilder.name("wiki.stats.vandalism")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .config("compression.type", "snappy")
                .build();
    }
}