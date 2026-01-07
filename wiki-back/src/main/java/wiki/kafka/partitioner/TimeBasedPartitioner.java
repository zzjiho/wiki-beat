package wiki.kafka.partitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TimeBasedPartitioner implements Partitioner {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int partitionCount = partitions.size();
        
        if (key == null) {
            return Utils.toPositive(Utils.murmur2(valueBytes)) % partitionCount;
        }
        
        String keyStr = key.toString();
        
        // Time-based partitioning (24-hour cycle)
        if (topic.equals("wiki.stats.minute")) {
            int hour = extractHourFromKey(keyStr);
            return hour % partitionCount;
        }

        // Default hash-based partitioning
        return Utils.toPositive(Utils.murmur2(keyBytes)) % partitionCount;
    }
    
    private int extractHourFromKey(String key) {
        try {
            // Extract hour from minute:2025-01-10:14:30 format
            if (key.startsWith("minute:")) {
                String timeStr = key.substring(7);
                String[] parts = timeStr.split(":");
                if (parts.length >= 2) {
                    return Integer.parseInt(parts[1]);
                }
            }

            return LocalDateTime.now().getHour();

        } catch (Exception e) {
            return LocalDateTime.now().getHour();
        }
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