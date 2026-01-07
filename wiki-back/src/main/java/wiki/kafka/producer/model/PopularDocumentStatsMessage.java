package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Popular document statistics message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularDocumentStatsMessage {

    private List<DocumentInfo> globalTop10;

    private List<DocumentInfo> englishTop10;

    private List<DocumentInfo> koreanTop10;

    private Integer windowMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
