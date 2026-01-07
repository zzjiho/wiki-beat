package wiki.domain.edit.dto.response;

/**
 * Stream status API response DTO
 */
public record StreamStatusResponse(
        boolean isStreaming,
        String streamEndpoint,
        ProcessingStats processing,
        ProducerStats producer,
        String message
) {

    public record ProcessingStats(
            long totalProcessed,
            long totalErrors,
            double errorRate
    ) {}

    public record ProducerStats(
            long totalSent,
            long totalErrors,
            double successRate
    ) {}

    public static StreamStatusResponse of(
            boolean isStreaming,
            long totalProcessed,
            long totalErrors,
            long producerSent,
            long producerErrors,
            double producerSuccessRate
    ) {
        long total = totalProcessed + totalErrors;
        double errorRate = total == 0 ? 0.0 : (double) totalErrors / total * 100.0;

        return new StreamStatusResponse(
                isStreaming,
                "https://stream.wikimedia.org/v2/stream/recentchange",
                new ProcessingStats(totalProcessed, totalErrors, errorRate),
                new ProducerStats(producerSent, producerErrors, producerSuccessRate),
                isStreaming ? "Streaming is running" : "Streaming is stopped"
        );
    }
}
