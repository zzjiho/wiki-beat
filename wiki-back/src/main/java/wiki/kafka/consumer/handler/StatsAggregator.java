package wiki.kafka.consumer.handler;

import wiki.kafka.producer.model.EditMessage;
import wiki.kafka.producer.model.MinuteStatsMessage;
import wiki.kafka.producer.model.LanguageStatsMessage;
import wiki.kafka.producer.model.TotalStatsMessage;
import wiki.kafka.producer.model.PopularDocumentStatsMessage;
import wiki.kafka.producer.model.SizeStatsMessage;
import wiki.kafka.producer.model.DocumentInfo;
import wiki.kafka.producer.model.NewDocumentStatsMessage;
import wiki.kafka.producer.model.NewDocumentInfo;
import wiki.kafka.producer.model.VandalismStatsMessage;
import wiki.kafka.producer.model.RevertInfo;
import wiki.kafka.producer.service.StatsProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsAggregator {
    
    private final StatsProducerService statsProducerService;
    
    // Per-minute statistics aggregation
    private final Map<String, MinuteStats> minuteStatsMap = new ConcurrentHashMap<>();

    // Per-language statistics aggregation
    private final Map<String, LanguageStats> languageStatsMap = new ConcurrentHashMap<>();

    // Total statistics
    private final AtomicLong totalEditCount = new AtomicLong(0);
    private final AtomicLong totalBotCount = new AtomicLong(0);
    private final AtomicLong totalNewPages = new AtomicLong(0);
    private final AtomicLong totalMinorEdits = new AtomicLong(0);
    private final AtomicLong totalSizeChange = new AtomicLong(0);

    // Popular document tracking (last 5 minutes)
    private final Map<DocumentKey, RecentDocumentStats> recentDocumentMap = new ConcurrentHashMap<>();

    // Edit size analysis
    private final AtomicInteger maxAddition = new AtomicInteger(Integer.MIN_VALUE);
    private final AtomicReference<String> maxAdditionTitle = new AtomicReference<>("");
    private final AtomicReference<String> maxAdditionLanguage = new AtomicReference<>("");

    private final AtomicInteger maxDeletion = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicReference<String> maxDeletionTitle = new AtomicReference<>("");
    private final AtomicReference<String> maxDeletionLanguage = new AtomicReference<>("");

    private final AtomicLong smallEditCount = new AtomicLong(0);   // -100 ~ +100
    private final AtomicLong mediumEditCount = new AtomicLong(0);  // +100 ~ +1000
    private final AtomicLong largeEditCount = new AtomicLong(0);   // +1000 or more

    // New document tracking (last 5 minutes)
    private final ConcurrentLinkedDeque<NewDocumentInfo> recentNewDocuments = new ConcurrentLinkedDeque<>();

    // Vandalism & revert tracking (last 5 minutes)
    private final ConcurrentLinkedDeque<RevertInfo> recentReverts = new ConcurrentLinkedDeque<>();
    
    /**
     * Aggregates edit data
     */
    public void aggregateEdit(EditMessage editMessage) {
        LocalDateTime editTime = editMessage.getTimestamp();
        String language = editMessage.getLanguage();

        // Normalize time to minute level
        LocalDateTime minute = editTime.truncatedTo(ChronoUnit.MINUTES);
        String minuteKey = minute.toString();

        // Update per-minute statistics
        minuteStatsMap.computeIfAbsent(minuteKey, k -> new MinuteStats(minute))
                .addEdit(editMessage);

        // Update per-language statistics
        languageStatsMap.computeIfAbsent(language, k -> new LanguageStats(language))
                .addEdit(editMessage);

        // Update total statistics
        totalEditCount.incrementAndGet();
        if (editMessage.isBotEdit()) {
            totalBotCount.incrementAndGet();
        }
        if (editMessage.isNewPage()) {
            totalNewPages.incrementAndGet();
        }
        if (editMessage.isMinorEdit()) {
            totalMinorEdits.incrementAndGet();
        }
        if (editMessage.getSizeDiff() != null) {
            totalSizeChange.addAndGet(editMessage.getSizeDiff());
        }

        // Track popular documents (last 5 minutes)
        DocumentKey docKey = new DocumentKey(editMessage.getTitle(), editMessage.getLanguage());
        RecentDocumentStats docStats = recentDocumentMap.computeIfAbsent(docKey,
                k -> new RecentDocumentStats(editMessage.getTitle(), editMessage.getLanguage(), editMessage.getUrl()));
        docStats.addEdit(editTime);

        // Log Korean document additions
        if ("ko".equals(editMessage.getLanguage())) {
            log.info("📌 [Map Add] Korean doc added: {} | total ko docs in map: {}",
                    editMessage.getTitle(),
                    recentDocumentMap.keySet().stream().filter(k -> "ko".equals(k.getLanguage())).count());
        }

        // Analyze edit size
        if (editMessage.getSizeDiff() != null) {
            int sizeDiff = editMessage.getSizeDiff();

            // Track maximum addition
            if (sizeDiff > 0 && sizeDiff > maxAddition.get()) {
                maxAddition.set(sizeDiff);
                maxAdditionTitle.set(editMessage.getTitle());
                maxAdditionLanguage.set(editMessage.getLanguage());
            }

            // Track maximum deletion
            if (sizeDiff < 0 && sizeDiff < maxDeletion.get()) {
                maxDeletion.set(sizeDiff);
                maxDeletionTitle.set(editMessage.getTitle());
                maxDeletionLanguage.set(editMessage.getLanguage());
            }

            // Classify by size range
            if (sizeDiff >= -100 && sizeDiff <= 100) {
                smallEditCount.incrementAndGet();
            } else if (sizeDiff > 100 && sizeDiff <= 1000) {
                mediumEditCount.incrementAndGet();
            } else if (sizeDiff > 1000) {
                largeEditCount.incrementAndGet();
            }
        }

        // Track new documents
        if (editMessage.isNewPage()) {
            NewDocumentInfo newDoc = NewDocumentInfo.builder()
                    .title(editMessage.getTitle())
                    .language(editMessage.getLanguage())
                    .creator(editMessage.getUser())
                    .isBot(editMessage.isBotEdit())
                    .url(editMessage.getUrl())
                    .createdTime(editTime)
                    .build();
            recentNewDocuments.add(newDoc);
        }

        // Detect vandalism & reverts (comment keyword analysis)
        String comment = editMessage.getComment();
        if (comment != null && !comment.isEmpty()) {
            String lowerComment = comment.toLowerCase();
            String revertType = null;

            if (lowerComment.contains("revert")) {
                revertType = "revert";
            } else if (lowerComment.contains("undo")) {
                revertType = "undo";
            } else if (lowerComment.contains("rollback")) {
                revertType = "rollback";
            } else if (lowerComment.contains("vandal")) {
                revertType = "vandalism";
            }

            if (revertType != null) {
                RevertInfo revertInfo = RevertInfo.builder()
                        .title(editMessage.getTitle())
                        .language(editMessage.getLanguage())
                        .revertType(revertType)
                        .user(editMessage.getUser())
                        .comment(comment)
                        .url(editMessage.getUrl())
                        .revertTime(editTime)
                        .build();
                recentReverts.add(revertInfo);
            }
        }
    }
    
    /**
     * Publishes per-minute statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishMinuteStats() {
        log.info("📊 [publishMinuteStats] START");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMinute = now.minusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        String lastMinuteKey = lastMinute.toString();

        // Debug logging
        log.info("📊 [publishMinuteStats] now={}, lastMinuteKey={}, mapKeys={}",
                now, lastMinuteKey, minuteStatsMap.keySet());

        MinuteStats stats = minuteStatsMap.remove(lastMinuteKey);

        // Publish with 0 values when no edits (maintain graph continuity)
        MinuteStatsMessage message;
        if (stats != null) {
            message = MinuteStatsMessage.builder()
                    .minute(lastMinute)
                    .editCount(stats.getEditCount())
                    .botEditCount(stats.getBotEditCount())
                    .newPageCount(stats.getNewPageCount())
                    .minorEditCount(stats.getMinorEditCount())
                    .totalSizeChange(stats.getTotalSizeChange())
                    .averageSizeChange(stats.getAverageSizeChange())
                    .languageDistribution(stats.getLanguageDistribution())
                    .timestamp(now)
                    .build();
        } else {
            // Fill with zeros when no edits
            message = MinuteStatsMessage.builder()
                    .minute(lastMinute)
                    .editCount(0L)
                    .botEditCount(0L)
                    .newPageCount(0L)
                    .minorEditCount(0L)
                    .totalSizeChange(0L)
                    .averageSizeChange(0.0)
                    .languageDistribution(new java.util.HashMap<>())
                    .timestamp(now)
                    .build();
        }

        statsProducerService.sendMinuteStats(message);

        // Cleanup old data (older than 5 minutes)
        LocalDateTime cutoff = now.minusMinutes(5);
        minuteStatsMap.entrySet().removeIf(entry -> 
                LocalDateTime.parse(entry.getKey()).isBefore(cutoff));
    }
    
    /**
     * Publishes per-language statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishLanguageStats() {
        log.info("📊 [publishLanguageStats] START");
        LocalDateTime now = LocalDateTime.now();
        
        languageStatsMap.forEach((language, stats) -> {
            if (stats.getEditCount() > 0) {
                LanguageStatsMessage message = LanguageStatsMessage.builder()
                        .language(language)
                        .editCount(stats.getEditCount())
                        .botEditCount(stats.getBotEditCount())
                        .userCount(stats.getUserCount())
                        .pageCount(stats.getPageCount())
                        .wikiDistribution(stats.getWikiDistribution())
                        .hourlyDistribution(stats.getHourlyDistribution())
                        .timestamp(now)
                        .build();
                
                statsProducerService.sendLanguageStats(message);
            }
        });
    }
    
    /**
     * Publishes total statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishTotalStats() {
        log.info("📊 [publishTotalStats] START");
        LocalDateTime now = LocalDateTime.now();

        // Calculate top 10 languages
        Map<String, Long> topLanguages = languageStatsMap.entrySet().stream()
                .sorted(Map.Entry.<String, LanguageStats>comparingByValue(
                        (a, b) -> Long.compare(b.getEditCount(), a.getEditCount())))
                .limit(10)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getEditCount(),
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new));
        
        TotalStatsMessage message = TotalStatsMessage.builder()
                .totalCount(totalEditCount.get())
                .totalBotCount(totalBotCount.get())
                .totalNewPages(totalNewPages.get())
                .totalMinorEdits(totalMinorEdits.get())
                .totalSizeChange(totalSizeChange.get())
                .topLanguages(topLanguages)
                .timestamp(now)
                .build();
        
        statsProducerService.sendTotalStats(message);
    }

    /**
     * Publishes popular document statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishPopularDocumentStats() {
        log.info("📊 [publishPopularDocumentStats] START");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusMinutes(5);

        // Global Top 10 (all languages)
        List<DocumentInfo> globalTop10 = getTop10Documents(null, cutoffTime);

        // English Top 10
        List<DocumentInfo> englishTop10 = getTop10Documents("en", cutoffTime);

        // Korean Top 10
        List<DocumentInfo> koreanTop10 = getTop10Documents("ko", cutoffTime);

        // Debug: Check Korean document count
        long koDocCount = recentDocumentMap.entrySet().stream()
                .filter(e -> "ko".equals(e.getKey().getLanguage()))
                .filter(e -> e.getValue().getRecentEditCount(cutoffTime) > 0)
                .count();

        log.info("🔍 [DEBUG] Korean documents in map: {}, with recent edits: {}, Top10 result: {}",
                recentDocumentMap.entrySet().stream().filter(e -> "ko".equals(e.getKey().getLanguage())).count(),
                koDocCount,
                koreanTop10.size());

        // Skip publishing if no data
        if (globalTop10.isEmpty() && englishTop10.isEmpty() && koreanTop10.isEmpty()) {
            log.debug("No popular documents in recent 5 minutes, skipping publish");
            return;
        }

        PopularDocumentStatsMessage message = PopularDocumentStatsMessage.builder()
                .globalTop10(globalTop10)
                .englishTop10(englishTop10)
                .koreanTop10(koreanTop10)
                .windowMinutes(5)
                .timestamp(now)
                .build();

        statsProducerService.sendPopularDocumentStats(message);
    }

    /**
     * Publishes edit size statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishSizeStats() {
        LocalDateTime now = LocalDateTime.now();

        long totalEdits = totalEditCount.get();
        long small = smallEditCount.get();
        long medium = mediumEditCount.get();
        long large = largeEditCount.get();

        // Calculate percentages
        double smallPercentage = totalEdits > 0 ? (small * 100.0 / totalEdits) : 0.0;
        double mediumPercentage = totalEdits > 0 ? (medium * 100.0 / totalEdits) : 0.0;
        double largePercentage = totalEdits > 0 ? (large * 100.0 / totalEdits) : 0.0;

        // Calculate average size
        double averageSize = totalEdits > 0 ? (totalSizeChange.get() * 1.0 / totalEdits) : 0.0;

        SizeStatsMessage message = SizeStatsMessage.builder()
                .averageSize(averageSize)
                .maxAddition(maxAddition.get() != Integer.MIN_VALUE ? maxAddition.get() : 0)
                .maxAdditionTitle(maxAdditionTitle.get())
                .maxAdditionLanguage(maxAdditionLanguage.get())
                .maxDeletion(maxDeletion.get() != Integer.MAX_VALUE ? maxDeletion.get() : 0)
                .maxDeletionTitle(maxDeletionTitle.get())
                .maxDeletionLanguage(maxDeletionLanguage.get())
                .smallEditPercentage(smallPercentage)
//                .mediumEditPercentage(mediumPercentage)
                .largeEditPercentage(largePercentage)
                .smallEditCount(small)
                .mediumEditCount(medium)
                .largeEditCount(large)
                .totalEditCount(totalEdits)
                .totalSizeChange(totalSizeChange.get())
                .timestamp(now)
                .build();

        statsProducerService.sendSizeStats(message);
    }

    /**
     * Publishes new document statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishNewDocumentStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusMinutes(5);

        // Remove data older than 5 minutes
        recentNewDocuments.removeIf(doc -> doc.getCreatedTime().isBefore(cutoffTime));

        // Total new document count
        long totalNewDocs = recentNewDocuments.size();

        if (totalNewDocs == 0) {
            return; // Skip if no data
        }

        // Aggregate by language
        Map<String, Long> byLanguage = recentNewDocuments.stream()
                .collect(Collectors.groupingBy(
                        NewDocumentInfo::getLanguage,
                        Collectors.counting()
                ));

        // Aggregate bot vs human
        long botCreated = recentNewDocuments.stream()
                .filter(doc -> Boolean.TRUE.equals(doc.getIsBot()))
                .count();
        long userCreated = totalNewDocs - botCreated;

        // Recent 20 documents (reverse chronological)
        List<NewDocumentInfo> recentDocs = recentNewDocuments.stream()
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .limit(20)
                .collect(Collectors.toList());

        NewDocumentStatsMessage message = NewDocumentStatsMessage.builder()
                .totalNewDocuments(totalNewDocs)
                .byLanguage(byLanguage)
                .botCreatedCount(botCreated)
                .userCreatedCount(userCreated)
                .recentDocuments(recentDocs)
                .windowMinutes(5)
                .timestamp(now)
                .build();

        statsProducerService.sendNewDocumentStats(message);
    }

    /**
     * Publishes vandalism & revert statistics every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void publishVandalismStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusMinutes(5);

        // Remove data older than 5 minutes
        recentReverts.removeIf(revert -> revert.getRevertTime().isBefore(cutoffTime));

        // Total revert count
        long totalReverts = recentReverts.size();

        if (totalReverts == 0) {
            return; // Skip if no data
        }

        // Aggregate by type
        Map<String, Long> byType = recentReverts.stream()
                .collect(Collectors.groupingBy(
                        RevertInfo::getRevertType,
                        Collectors.counting()
                ));

        // Aggregate revert counts per document (using "|||" as delimiter since titles may contain colons)
        Map<String, Long> documentRevertCounts = recentReverts.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTitle() + "|||" + r.getLanguage(),
                        Collectors.counting()
                ));

        // Top 5 most reverted documents
        List<VandalismStatsMessage.DocumentRevertCount> topRevertedDocs = documentRevertCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|\\|\\|", 2);
                    String title = parts[0];
                    String language = parts.length > 1 ? parts[1] : "unknown";

                    String url = recentReverts.stream()
                            .filter(r -> r.getTitle().equals(title) && r.getLanguage().equals(language))
                            .findFirst()
                            .map(RevertInfo::getUrl)
                            .orElse(null);

                    return VandalismStatsMessage.DocumentRevertCount.builder()
                            .title(title)
                            .language(language)
                            .revertCount(entry.getValue())
                            .url(url)
                            .build();
                })
                .collect(Collectors.toList());

        // Recent 20 reverts (reverse chronological)
        List<RevertInfo> recentRevertsList = recentReverts.stream()
                .sorted((a, b) -> b.getRevertTime().compareTo(a.getRevertTime()))
                .limit(20)
                .collect(Collectors.toList());

        VandalismStatsMessage message = VandalismStatsMessage.builder()
                .totalReverts(totalReverts)
                .byType(byType)
                .mostRevertedDocuments(topRevertedDocs)
                .recentReverts(recentRevertsList)
                .windowMinutes(5)
                .timestamp(now)
                .build();

        statsProducerService.sendVandalismStats(message);
    }

    /**
     * Cleans up old document data every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupOldDocuments() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);

        int beforeSize = recentDocumentMap.size();

        recentDocumentMap.forEach((key, stats) -> stats.cleanupOldTimestamps(cutoffTime));

        recentDocumentMap.entrySet().removeIf(entry ->
                entry.getValue().getRecentEditCount(cutoffTime) == 0
        );

        int afterSize = recentDocumentMap.size();
    }

    /**
     * Retrieves Top 10 documents with optional language filter
     */
    private List<DocumentInfo> getTop10Documents(String languageFilter, LocalDateTime cutoffTime) {
        List<DocumentInfo> result = recentDocumentMap.entrySet().stream()
                .filter(e -> {
                    String lang = e.getKey().getLanguage();
                    if ("wikidata".equals(lang)) {
                        return false;
                    }
                    return languageFilter == null || lang.equals(languageFilter);
                })
                .map(e -> {
                    RecentDocumentStats stats = e.getValue();
                    long count = stats.getRecentEditCount(cutoffTime);
                    return count > 0 ? DocumentInfo.builder()
                            .title(stats.getTitle())
                            .language(stats.getLanguage())
                            .editCount(count)
                            .lastEditTime(stats.getLastEditTime())
                            .url(stats.getUrl())
                            .build() : null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Long.compare(b.getEditCount(), a.getEditCount()))
                .limit(10)
                .collect(Collectors.toList());

        if (languageFilter != null && result.size() < 10) {
            log.warn("⚠️ [DEBUG] getTop10Documents({}) returned only {} documents", languageFilter, result.size());
        }

        return result;
    }

    // Internal statistics classes
    private static class MinuteStats {
        private final LocalDateTime minute;
        private final LongAdder editCount = new LongAdder();
        private final LongAdder botEditCount = new LongAdder();
        private final LongAdder newPageCount = new LongAdder();
        private final LongAdder minorEditCount = new LongAdder();
        private final LongAdder totalSizeChange = new LongAdder();
        private final Map<String, LongAdder> languageDistribution = new ConcurrentHashMap<>();

        public MinuteStats(LocalDateTime minute) {
            this.minute = minute;
        }

        public void addEdit(EditMessage edit) {
            editCount.increment();
            if (edit.isBotEdit()) botEditCount.increment();
            if (edit.isNewPage()) newPageCount.increment();
            if (edit.isMinorEdit()) minorEditCount.increment();
            if (edit.getSizeDiff() != null) totalSizeChange.add(edit.getSizeDiff());

            // Track language distribution
            languageDistribution.computeIfAbsent(edit.getLanguage(), k -> new LongAdder()).increment();
        }
        
        public long getEditCount() { return editCount.sum(); }
        public long getBotEditCount() { return botEditCount.sum(); }
        public long getNewPageCount() { return newPageCount.sum(); }
        public long getMinorEditCount() { return minorEditCount.sum(); }
        public long getTotalSizeChange() { return totalSizeChange.sum(); }

        public Map<String, Long> getLanguageDistribution() {
            return languageDistribution.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().sum()));
        }

        public double getAverageSizeChange() {
            long count = editCount.sum();
            return count > 0 ? (double) totalSizeChange.sum() / count : 0.0;
        }
    }
    
    private static class LanguageStats {
        private final String language;
        private final LongAdder editCount = new LongAdder();
        private final LongAdder botEditCount = new LongAdder();
        private final Map<String, LongAdder> userCount = new ConcurrentHashMap<>();
        private final Map<String, LongAdder> pageCount = new ConcurrentHashMap<>();
        private final Map<String, LongAdder> wikiDistribution = new ConcurrentHashMap<>();
        private final Map<Integer, LongAdder> hourlyDistribution = new ConcurrentHashMap<>();
        
        public LanguageStats(String language) {
            this.language = language;
        }
        
        public void addEdit(EditMessage edit) {
            editCount.increment();
            if (edit.isBotEdit()) botEditCount.increment();
            
            userCount.computeIfAbsent(edit.getUser(), k -> new LongAdder()).increment();
            pageCount.computeIfAbsent(edit.getTitle(), k -> new LongAdder()).increment();
            wikiDistribution.computeIfAbsent(edit.getWiki(), k -> new LongAdder()).increment();
            
            int hour = edit.getTimestamp().getHour();
            hourlyDistribution.computeIfAbsent(hour, k -> new LongAdder()).increment();
        }
        
        public long getEditCount() { return editCount.sum(); }
        public long getBotEditCount() { return botEditCount.sum(); }
        public long getUserCount() { return userCount.size(); }
        public long getPageCount() { return pageCount.size(); }
        
        public Map<String, Long> getWikiDistribution() {
            return wikiDistribution.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().sum()));
        }
        
        public Map<Integer, Long> getHourlyDistribution() {
            return hourlyDistribution.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().sum()));
        }
    }

    /**
     * Document unique key (title + language)
     */
    private static class DocumentKey {
        private final String title;
        private final String language;

        public DocumentKey(String title, String language) {
            this.title = title;
            this.language = language;
        }

        public String getTitle() {
            return title;
        }

        public String getLanguage() {
            return language;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DocumentKey that = (DocumentKey) o;
            return Objects.equals(title, that.title) && Objects.equals(language, that.language);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, language);
        }
    }

    /**
     * Recent document edit statistics (tracks timestamps from last 5 minutes)
     */
    private static class RecentDocumentStats {
        private final String title;
        private final String language;
        private final String url;
        private final ConcurrentLinkedDeque<LocalDateTime> editTimestamps = new ConcurrentLinkedDeque<>();

        public RecentDocumentStats(String title, String language, String url) {
            this.title = title;
            this.language = language;
            this.url = url;
        }

        public void addEdit(LocalDateTime timestamp) {
            editTimestamps.add(timestamp);
        }

        public long getRecentEditCount(LocalDateTime cutoffTime) {
            return editTimestamps.stream()
                    .filter(t -> !t.isBefore(cutoffTime))
                    .count();
        }

        public void cleanupOldTimestamps(LocalDateTime cutoffTime) {
            editTimestamps.removeIf(t -> t.isBefore(cutoffTime));
        }

        public String getTitle() {
            return title;
        }

        public String getLanguage() {
            return language;
        }

        public String getUrl() {
            return url;
        }

        public LocalDateTime getLastEditTime() {
            return editTimestamps.peekLast();
        }
    }
}