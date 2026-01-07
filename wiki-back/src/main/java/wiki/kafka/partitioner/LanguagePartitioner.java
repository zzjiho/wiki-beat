package wiki.kafka.partitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguagePartitioner implements Partitioner {
    
    private static final Map<String, Integer> LANGUAGE_PARTITION_MAP = new HashMap<>();

    static {
        LANGUAGE_PARTITION_MAP.put("en", 0);
        LANGUAGE_PARTITION_MAP.put("zh", 1);
        LANGUAGE_PARTITION_MAP.put("ja", 2);
        LANGUAGE_PARTITION_MAP.put("es", 3);
        LANGUAGE_PARTITION_MAP.put("fr", 4);
        LANGUAGE_PARTITION_MAP.put("de", 5);
        LANGUAGE_PARTITION_MAP.put("ru", 6);
        LANGUAGE_PARTITION_MAP.put("ko", 7);
        LANGUAGE_PARTITION_MAP.put("ar", 8);
        LANGUAGE_PARTITION_MAP.put("pt", 9);
    }
    
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int partitionCount = partitions.size();
        
        if (key == null) {
            return Utils.toPositive(Utils.murmur2(valueBytes)) % partitionCount;
        }
        
        String keyStr = key.toString();

        String language = extractLanguage(keyStr);

        if (LANGUAGE_PARTITION_MAP.containsKey(language)) {
            int fixedPartition = LANGUAGE_PARTITION_MAP.get(language);
            return fixedPartition % partitionCount;
        }

        return Utils.toPositive(Utils.murmur2(language.getBytes())) % partitionCount;
    }
    
    private String extractLanguage(String key) {
        String[] parts = key.split(":");
        return parts.length > 0 ? parts[0] : "unknown";
    }
    
    @Override
    public void close() {
        // cleanup resources if needed
    }
    
    @Override
    public void configure(Map<String, ?> configs) {
        // configuration if needed
    }
}