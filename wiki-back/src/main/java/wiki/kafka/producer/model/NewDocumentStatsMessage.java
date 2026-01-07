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
 * New document creation statistics message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewDocumentStatsMessage {

    private Long totalNewDocuments;

    private Map<String, Long> byLanguage;

    private Long botCreatedCount;

    private Long userCreatedCount;

    private List<NewDocumentInfo> recentDocuments;

    private Integer windowMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
