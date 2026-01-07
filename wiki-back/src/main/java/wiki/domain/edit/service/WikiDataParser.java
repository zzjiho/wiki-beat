package wiki.domain.edit.service;

import wiki.kafka.producer.model.EditMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class WikiDataParser {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Validates Wikimedia event stream data
     */
    public boolean isValidEventData(String data) {
        return data != null && 
               !data.trim().isEmpty() && 
               !data.startsWith(":") && 
               data.startsWith("data: ");
    }
    
    /**
     * Converts event data to EditMessage object
     */
    public EditMessage parseToEditMessage(String eventData) {
        try {
            // Remove "data: " prefix
            String jsonData = eventData.substring(6);
            JsonNode node = objectMapper.readTree(jsonData);
            
            // Check if edit event
            if (!isEditEvent(node)) {
                return null;
            }
            
            // Build EditMessage object
            return EditMessage.builder()
                    .title(getStringValue(node, "title"))
                    .user(getStringValue(node, "user"))
                    .wiki(getStringValue(node, "wiki"))
                    .language(extractLanguageFromWiki(getStringValue(node, "wiki")))
                    .editType(getStringValue(node, "type"))
                    .sizeDiff(getIntValue(node, "length", "new") - getIntValue(node, "length", "old"))
                    .isBot(getBooleanValue(node, "bot"))
                    .isMinor(getBooleanValue(node, "minor"))
                    .isNew("new".equals(getStringValue(node, "type")))
                    .comment(getStringValue(node, "comment"))
                    .url(getStringValue(node, "meta", "uri"))
                    .timestamp(parseTimestamp(getStringValue(node, "timestamp")))
                    .messageId(generateMessageId(node))
                    .namespace(getStringValue(node, "namespace"))
                    .serverName(getStringValue(node, "server_name"))
                    .revisionId(getLongValue(node, "revision", "new"))
                    .oldRevisionId(getLongValue(node, "revision", "old"))
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse event data: {}", eventData, e);
            return null;
        }
    }
    
    // Check if edit event (only edit or new types)
    private boolean isEditEvent(JsonNode node) {
        String type = getStringValue(node, "type");
        return "edit".equals(type) || "new".equals(type);
    }
    
    // Extract string value from JSON
    private String getStringValue(JsonNode node, String... path) {
        JsonNode current = node;
        for (String key : path) {
            if (current == null || !current.has(key)) {
                return "";
            }
            current = current.get(key);
        }
        return current != null ? current.asText("") : "";
    }
    
    // Extract integer value from JSON
    private int getIntValue(JsonNode node, String... path) {
        JsonNode current = node;
        for (String key : path) {
            if (current == null || !current.has(key)) {
                return 0;
            }
            current = current.get(key);
        }
        return current != null ? current.asInt(0) : 0;
    }
    
    // Extract long value from JSON
    private long getLongValue(JsonNode node, String... path) {
        JsonNode current = node;
        for (String key : path) {
            if (current == null || !current.has(key)) {
                return 0L;
            }
            current = current.get(key);
        }
        return current != null ? current.asLong(0L) : 0L;
    }
    
    // Extract boolean value from JSON
    private boolean getBooleanValue(JsonNode node, String key) {
        return node.has(key) && node.get(key).asBoolean(false);
    }
    
    // Extract language from wiki name (e.g., enwiki → en, ko.wikipedia.org → ko)
    private String extractLanguageFromWiki(String wiki) {
        if (wiki == null || wiki.isEmpty()) {
            return "unknown";
        }

        // 1. Handle "enwiki", "kowiki" format
        if (wiki.endsWith("wiki")) {
            return wiki.substring(0, wiki.length() - 4);
        }

        // 2. Handle "en.wikipedia.org" domain format
        int dotIndex = wiki.indexOf('.');
        if (dotIndex > 0) {
            return wiki.substring(0, dotIndex);
        }

        return "unknown";
    }
    
    // Convert timestamp string to LocalDateTime
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            if (timestamp == null || timestamp.isEmpty()) {
                return LocalDateTime.now();
            }
            
            // Check if Unix timestamp (seconds)
            if (timestamp.matches("\\d+")) {
                long epochSeconds = Long.parseLong(timestamp);
                Instant instant = Instant.ofEpochSecond(epochSeconds);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
            
            // Try ISO 8601 format
            Instant instant = Instant.parse(timestamp);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", timestamp, e);
            return LocalDateTime.now();
        }
    }
    
    // Generate unique message ID for deduplication
    private String generateMessageId(JsonNode node) {
        String wiki = getStringValue(node, "wiki");
        long revisionId = getLongValue(node, "revision", "new");
        String timestamp = getStringValue(node, "timestamp");
        
        return String.format("%s:%d:%s", wiki, revisionId, timestamp);
    }
}