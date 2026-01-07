package wiki.domain.edit.dto.response;

import wiki.kafka.producer.model.DocumentInfo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Popular documents API response DTO
 */
public record PopularDocumentsResponse(
        List<DocumentInfo> globalTop10,
        List<DocumentInfo> englishTop10,
        List<DocumentInfo> koreanTop10,
        Integer windowMinutes,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public static PopularDocumentsResponse from(wiki.kafka.producer.model.PopularDocumentStatsMessage message) {
        if (message == null) {
            return new PopularDocumentsResponse(
                    List.of(),
                    List.of(),
                    List.of(),
                    5,
                    LocalDateTime.now()
            );
        }
        return new PopularDocumentsResponse(
                message.getGlobalTop10(),
                message.getEnglishTop10(),
                message.getKoreanTop10(),
                message.getWindowMinutes(),
                message.getTimestamp()
        );
    }
}
