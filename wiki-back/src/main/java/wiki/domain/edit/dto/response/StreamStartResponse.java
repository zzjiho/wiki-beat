package wiki.domain.edit.dto.response;

/**
 * Stream start API response DTO
 */
public record StreamStartResponse(
        String message,
        StreamStatus status,
        Long totalProcessed,
        Long totalErrors,
        KafkaTopicInfo kafkaTopics
) {

    public enum StreamStatus {
        STARTED,
        ALREADY_RUNNING,
        FAILED
    }

    public record KafkaTopicInfo(
            String rawEdits,
            String minuteStats,
            String languageStats,
            String totalStats
    ) {

        public static KafkaTopicInfo createDefault() {
            return new KafkaTopicInfo(
                    "wiki.raw.edits",
                    "wiki.stats.minute",
                    "wiki.stats.language",
                    "wiki.stats.total"
            );
        }
    }

    public static StreamStartResponse alreadyRunning(long totalProcessed, long totalErrors) {
        return new StreamStartResponse(
                "Streaming is already running",
                StreamStatus.ALREADY_RUNNING,
                totalProcessed,
                totalErrors,
                null
        );
    }

    public static StreamStartResponse started() {
        return new StreamStartResponse(
                "Wikimedia streaming started successfully",
                StreamStatus.STARTED,
                0L,
                0L,
                KafkaTopicInfo.createDefault()
        );
    }

    public static StreamStartResponse failed(String errorMessage) {
        return new StreamStartResponse(
                "Failed to start streaming: " + errorMessage,
                StreamStatus.FAILED,
                null,
                null,
                null
        );
    }
}
