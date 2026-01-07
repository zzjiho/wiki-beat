package wiki.domain.edit.dto.response;

import wiki.kafka.producer.model.NewDocumentInfo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * New documents API response DTO
 */
public record NewDocumentsResponse(
        Long totalNewDocuments,
        Map<String, Long> byLanguage,
        Long botCreatedCount,
        Long userCreatedCount,
        List<NewDocumentInfo> recentDocuments,
        Integer windowMinutes,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public static NewDocumentsResponse from(wiki.kafka.producer.model.NewDocumentStatsMessage message) {
        if (message == null) {
            return new NewDocumentsResponse(
                    0L,
                    Map.of(),
                    0L,
                    0L,
                    List.of(),
                    5,
                    LocalDateTime.now()
            );
        }
        return new NewDocumentsResponse(
                message.getTotalNewDocuments(),
                message.getByLanguage(),
                message.getBotCreatedCount(),
                message.getUserCreatedCount(),
                message.getRecentDocuments(),
                message.getWindowMinutes(),
                message.getTimestamp()
        );
    }
}
