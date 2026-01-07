package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * New document information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewDocumentInfo {

    private String title;

    private String language;

    private String creator;

    private Boolean isBot;

    private String url;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTime;
}
