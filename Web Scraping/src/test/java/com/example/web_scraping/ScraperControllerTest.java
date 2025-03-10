package com.example.web_scraping;

import com.example.web_scraping.controller.ScraperController;
import com.example.web_scraping.service.WebScraperService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ScraperControllerTest {

    @Autowired
    private ScraperController controller;

    @Test
    void testScraping() {
        Map<String, Object> response = controller.scrape(Map.of(
                "urls", List.of("https://example.com"),
                "keywords", List.of("technology")
        ));
        assertEquals("success", response.get("status"));
    }

    @Test
    void testSearch() {
        // Perform scraping first to populate Trie
        controller.scrape(Map.of(
                "urls", List.of("https://example.com"),
                "keywords", List.of("technology")
        ));

        Map<String, Object> response = controller.search(Map.of(
                "prefix", "tech",
                "limit", 5
        ));

        assertEquals("success", response.get("status"));
    }
}
