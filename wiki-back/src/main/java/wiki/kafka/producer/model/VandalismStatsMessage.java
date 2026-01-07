package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Vandalism & revert activity statistics message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VandalismStatsMessage {

    private Long totalReverts;

    private Map<String, Long> byType;

    private List<DocumentRevertCount> mostRevertedDocuments;

    private List<RevertInfo> recentReverts;

    private Integer windowMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentRevertCount {
        private String title;
        private String language;
        private Long revertCount;
        private String url;
    }
}
