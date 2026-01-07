package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class TotalStatsMessage {
    
    private Long totalCount;
    private Long totalBotCount;
    private Long totalNewPages;
    private Long totalMinorEdits;
    
    private Map<String, Long> topLanguages;

    private Map<Integer, Long> hourlyStats;

    private Map<String, Long> dailyStats;
    
    private Long totalSizeChange;
    private Double averageEditsPerMinute;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonIgnore
    public Double getTotalBotRatio() {
        if (totalCount == 0) return 0.0;
        return (double) totalBotCount / totalCount * 100.0;
    }
    
    @JsonIgnore
    public Double getNewPageRatio() {
        if (totalCount == 0) return 0.0;
        return (double) totalNewPages / totalCount * 100.0;
    }
    
    @JsonIgnore
    public Double getMinorEditRatio() {
        if (totalCount == 0) return 0.0;
        return (double) totalMinorEdits / totalCount * 100.0;
    }
}