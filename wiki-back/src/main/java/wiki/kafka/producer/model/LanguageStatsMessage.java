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
public class LanguageStatsMessage {
    
    private String language;
    private Long editCount;
    private Long botEditCount;
    private Long userCount;
    private Long pageCount;
    
    private Map<String, Long> wikiDistribution;

    private Map<Integer, Long> hourlyDistribution;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public Double getBotEditRatio() {
        if (editCount == 0) return 0.0;
        return (double) botEditCount / editCount * 100.0;
    }
    
    public Double getEditsPerUser() {
        if (userCount == 0) return 0.0;
        return (double) editCount / userCount;
    }
    
    public Double getEditsPerPage() {
        if (pageCount == 0) return 0.0;
        return (double) editCount / pageCount;
    }
}