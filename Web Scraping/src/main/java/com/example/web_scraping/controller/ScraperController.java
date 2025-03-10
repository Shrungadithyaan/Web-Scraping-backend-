package com.example.web_scraping.controller;

import com.example.web_scraping.model.ScrapedData;
import com.example.web_scraping.service.WebScraperService;
import com.example.web_scraping.trie.Trie;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1")
public class ScraperController {

    private final WebScraperService webScraperService;
    private final Trie trie = new Trie();
    private final List<ScrapedData> scrapedDataList = new ArrayList<>();
    private static final AtomicInteger jobCounter = new AtomicInteger(1000); // Start job IDs from 1000

    public ScraperController(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }

    @PostMapping("/scrape")
    public Map<String, Object> scrape(@RequestBody Map<String, Object> request) {
        List<String> urls = new ArrayList<>((Collection<String>) request.getOrDefault("urls", new ArrayList<>()));
        List<String> keywords = new ArrayList<>((Collection<String>) request.getOrDefault("keywords", new ArrayList<>()));
        String scheduledAt = (String) request.getOrDefault("schedule", "Not Scheduled");

        int jobId = jobCounter.incrementAndGet(); // Generate a numeric job ID
        List<ScrapedData> scrapedData = webScraperService.scrapeData(urls, keywords);
        scrapedDataList.addAll(scrapedData);

        for (String keyword : keywords) {
            trie.insert(keyword);
        }

        return new LinkedHashMap<>() {{
            put("status", "success");
            put("message", "Scraping initiated successfully.");
            put("jobId", jobId);
            put("scheduledAt", scheduledAt);
        }};
    }

    @PostMapping("/search")

    public Map<String, Object> search(@RequestBody Map<String, Object> request) {
        String prefix = ((String) request.get("prefix")).toLowerCase();
        int limit = (int) request.getOrDefault("limit", 5);

        List<Map<String, Object>> results = new ArrayList<>();

        for (ScrapedData data : scrapedDataList) {
            String content = data.getContent().toLowerCase();
            int index = content.indexOf(prefix);

            if (index != -1 && results.size() < limit) {
                int snippetStart = Math.max(0, index - 30);  // Extract 30 chars before match
                int snippetEnd = Math.min(data.getContent().length(), index + prefix.length() + 50); // 50 chars after

                String matchedContent = data.getContent().substring(snippetStart, snippetEnd);

                // Ensure matchedContent starts at a full word
                if (snippetStart > 0) {
                    int firstSpace = matchedContent.indexOf(' ');
                    if (firstSpace != -1) {
                        matchedContent = matchedContent.substring(firstSpace + 1);
                    }
                }

                // Ensure matchedContent ends at a full word
                if (snippetEnd < data.getContent().length()) {
                    int lastSpace = matchedContent.lastIndexOf(' ');
                    if (lastSpace != -1) {
                        matchedContent = matchedContent.substring(0, lastSpace);
                    }
                }

                matchedContent = matchedContent.trim() + "...";

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("url", data.getUrl());
                result.put("matchedContent", matchedContent);
                result.put("timestamp", data.getTimestamp());
                results.add(result);
            }
        }

        return Map.of(
                "status", results.isEmpty() ? "failure" : "success",
                "results", results
        );
    }


    @GetMapping("/status/{jobId}")

    public Map<String, Object> getStatus(@PathVariable int jobId) {
        List<String> urlsScraped = new ArrayList<>();
        Set<String> keywordsFound = new HashSet<>();
        int totalSize = 0; // Data size in bytes

        for (ScrapedData data : scrapedDataList) {
            urlsScraped.add(data.getUrl());
            keywordsFound.addAll(data.getKeywordsFound());
            totalSize += data.getContent().getBytes().length;
        }

        double dataSizeInMB = totalSize / (1024.0 * 1024.0); // Convert bytes to MB
        String finishedAt = LocalDateTime.now().toString(); // Set completion timestamp

        return Map.of(
                "status", "completed",
                "jobId", jobId,
                "urlsScraped", urlsScraped,
                "keywordsFound", new ArrayList<>(keywordsFound),
                "dataSize", String.format("%.2f MB", dataSizeInMB),
                "finishedAt", finishedAt
        );
    }

}
