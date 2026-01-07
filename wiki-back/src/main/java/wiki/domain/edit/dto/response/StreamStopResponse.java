package wiki.domain.edit.dto.response;

/**
 * Stream stop API response DTO
 */
public record StreamStopResponse(
        String message,
        StopStatus status,
        FinalStats finalStats
) {

    public enum StopStatus {
        STOPPED,
        NOT_RUNNING,
        FAILED
    }

    public record FinalStats(
            Long totalProcessed,
            Long totalErrors,
            Long producerSent,
            Long producerErrors
    ) {}

    public static StreamStopResponse notRunning() {
        return new StreamStopResponse(
                "Streaming is not running",
                StopStatus.NOT_RUNNING,
                null
        );
    }

    public static StreamStopResponse stopped(long totalProcessed, long totalErrors,
                                             long producerSent, long producerErrors) {
        return new StreamStopResponse(
                "Wikimedia streaming stopped successfully",
                StopStatus.STOPPED,
                new FinalStats(totalProcessed, totalErrors, producerSent, producerErrors)
        );
    }

    public static StreamStopResponse failed(String errorMessage) {
        return new StreamStopResponse(
                "Failed to stop streaming: " + errorMessage,
                StopStatus.FAILED,
                null
        );
    }
}
