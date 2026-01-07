package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Edit size analysis statistics message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeStatsMessage {

    private Double averageSize;

    private Integer maxAddition;
    private String maxAdditionTitle;
    private String maxAdditionLanguage;

    private Integer maxDeletion;
    private String maxDeletionTitle;
    private String maxDeletionLanguage;

    private Double smallEditPercentage;    // -100 ~ +100 bytes
    private Double mediumEditPercentage;   // +100 ~ +1000 bytes
    private Double largeEditPercentage;    // +1000 bytes or more

    private Long smallEditCount;
    private Long mediumEditCount;
    private Long largeEditCount;

    private Long totalEditCount;

    private Long totalSizeChange;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
