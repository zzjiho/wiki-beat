package wiki.domain.edit.controller;

import wiki.api.RestApiResponse;
import wiki.domain.edit.dto.response.*;
import wiki.domain.edit.facade.BigDataStatsFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bigdata")
public class BigDataStatsController {

    private final BigDataStatsFacade bigDataStatsFacade;
    
    /**
     * Retrieves real-time dashboard data
     */
    @GetMapping("/dashboard")
    public RestApiResponse<DashboardResponse> getDashboard() {
        return RestApiResponse.success(bigDataStatsFacade.getDashboard());
    }

    /**
     * Retrieves total edit statistics (per-minute basis)
     */
    @GetMapping("/stats/total")
    public RestApiResponse<TotalStatsResponse> getTotalStats(
            @RequestParam(name = "minutes", defaultValue = "10") int minutes) {
        return RestApiResponse.success(bigDataStatsFacade.getTotalStats(minutes));
    }

    /**
     * Retrieves top 10 popular documents
     */
    @GetMapping("/popular-documents")
    public RestApiResponse<PopularDocumentsResponse> getPopularDocuments() {
        return RestApiResponse.success(bigDataStatsFacade.getPopularDocuments());
    }

    /**
     * Retrieves new document statistics (last 5 minutes)
     */
    @GetMapping("/new-documents")
    public RestApiResponse<NewDocumentsResponse> getNewDocumentStats() {
        return RestApiResponse.success(bigDataStatsFacade.getNewDocuments());
    }

    /**
     * Retrieves vandalism and revert statistics (last 5 minutes)
     */
    @GetMapping("/vandalism-stats")
    public RestApiResponse<VandalismStatsResponse> getVandalismStats() {
        return RestApiResponse.success(bigDataStatsFacade.getVandalismStats());
    }
}