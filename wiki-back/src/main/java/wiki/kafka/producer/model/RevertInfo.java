package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Revert edit information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevertInfo {

    private String title;

    private String language;

    private String revertType;

    private String user;

    private String comment;

    private String url;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime revertTime;
}
