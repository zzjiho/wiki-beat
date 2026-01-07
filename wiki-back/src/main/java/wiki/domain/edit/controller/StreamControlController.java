package wiki.domain.edit.controller;

import wiki.api.RestApiResponse;
import wiki.domain.edit.dto.response.StreamStartResponse;
import wiki.domain.edit.dto.response.StreamStatusResponse;
import wiki.domain.edit.dto.response.StreamStopResponse;
import wiki.domain.edit.facade.WikiStreamFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stream")
public class StreamControlController {
    
    private final WikiStreamFacade wikiStreamFacade;
    
    /**
     * Starts Wikimedia real-time streaming via SSE
     */
    @PostMapping("/start")
    public RestApiResponse<StreamStartResponse> startStream() {
        return RestApiResponse.success(wikiStreamFacade.startStream());
    }

    /**
     * Stops Wikimedia streaming
     */
    @PostMapping("/stop")
    public RestApiResponse<StreamStopResponse> stopStream() {
        return RestApiResponse.success(wikiStreamFacade.stopStream());
    }

    /**
     * Returns current streaming status
     */
    @GetMapping("/status")
    public RestApiResponse<StreamStatusResponse> getStreamStatus() {
        return RestApiResponse.success(wikiStreamFacade.getStreamStatus());
    }
}