package wiki.domain.edit.dto.response;

import wiki.kafka.producer.model.RevertInfo;
import wiki.kafka.producer.model.VandalismStatsMessage;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Vandalism statistics API response DTO
 */
public record VandalismStatsResponse(
        Long totalReverts,
        Map<String, Long> byType,
        List<VandalismStatsMessage.DocumentRevertCount> mostRevertedDocuments,
        List<RevertInfo> recentReverts,
        Integer windowMinutes,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public static VandalismStatsResponse from(VandalismStatsMessage message) {
        if (message == null) {
            return new VandalismStatsResponse(
                    0L,
                    Map.of(),
                    List.of(),
                    List.of(),
                    5,
                    LocalDateTime.now()
            );
        }
        return new VandalismStatsResponse(
                message.getTotalReverts(),
                message.getByType(),
                message.getMostRevertedDocuments(),
                message.getRecentReverts(),
                message.getWindowMinutes(),
                message.getTimestamp()
        );
    }
}
