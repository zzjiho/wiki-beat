package wiki.kafka.producer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditMessage {
    
    private String title;
    private String user;
    private String wiki;
    private String language;
    private String editType;
    private Integer sizeDiff;
    private Boolean isBot;
    private Boolean isMinor;
    private Boolean isNew;
    private String comment;
    private String url;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String messageId;

    private String namespace;
    private String serverName;
    private Long revisionId;
    private Long oldRevisionId;
    
    public static EditMessage fromWikiEvent(String eventJson) {
        return EditMessage.builder().build();
    }
    
    public String getPartitionKey() {
        return String.format("%s:%s", language, wiki);
    }
    
    public boolean isBotEdit() {
        return Boolean.TRUE.equals(isBot);
    }
    
    public boolean isNewPage() {
        return Boolean.TRUE.equals(isNew);
    }
    
    public boolean isMinorEdit() {
        return Boolean.TRUE.equals(isMinor);
    }
}