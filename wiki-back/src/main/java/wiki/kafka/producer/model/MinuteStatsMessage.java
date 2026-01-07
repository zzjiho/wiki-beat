package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinuteStatsMessage {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime minute;
    
    private Long editCount;
    private Long botEditCount;
    private Long newPageCount;
    private Long minorEditCount;
    
    private Long totalSizeChange;
    private Double averageSizeChange;

    private Map<String, Long> languageDistribution;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public Double getBotEditRatio() {
        if (editCount == 0) return 0.0;
        return (double) botEditCount / editCount * 100.0;
    }
    
    public Double getNewPageRatio() {
        if (editCount == 0) return 0.0;
        return (double) newPageCount / editCount * 100.0;
    }
    
    public Double getMinorEditRatio() {
        if (editCount == 0) return 0.0;
        return (double) minorEditCount / editCount * 100.0;
    }
}