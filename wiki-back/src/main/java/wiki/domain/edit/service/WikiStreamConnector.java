package wiki.domain.edit.service;

import wiki.exception.WikiStreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
@Slf4j
public class WikiStreamConnector {

    private static final String WIKIMEDIA_STREAM_URL = "https://stream.wikimedia.org/v2/stream/recentchange";

    /**
     * Connects to Wikimedia stream and processes data line by line
     */
    public void connectAndProcess(Consumer<String> lineProcessor, AtomicBoolean shouldContinue) {
        try {
            log.info("Connecting to Wikimedia stream...");

            // HTTP connection setup
            URL url = new URL(WIKIMEDIA_STREAM_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("User-Agent", "WikimediaBigDataDashboard/1.0 (https://github.com/wikimedia/bigdata; contact@example.com)");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new WikiStreamException("Wikimedia stream connection failed: " + connection.getResponseCode());
            }

            log.info("Wikimedia stream connected successfully!");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {

                String line;
                while (shouldContinue.get() && (line = reader.readLine()) != null) {
                    lineProcessor.accept(line);
                }
            }

        } catch (Exception e) {
            log.error("Error connecting to Wikimedia stream", e);
            throw new WikiStreamException("Stream connection error", e);
        }
    }
}