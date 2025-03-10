package com.example.web_scraping.service;

import com.example.web_scraping.model.ScrapedData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WebScraperService {

    public List<ScrapedData> scrapeData(List<String> urls, List<String> keywords) {
        List<ScrapedData> results = new ArrayList<>();

        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).get();
                String text = doc.text().toLowerCase(); // Convert to lowercase for case-insensitive search
                Set<String> matchedKeywords = new HashSet<>();

                for (String keyword : keywords) {
                    if (text.contains(keyword.toLowerCase())) {
                        matchedKeywords.add(keyword);
                    }
                }

                if (!matchedKeywords.isEmpty()) {
                    results.add(new ScrapedData(url, text, LocalDateTime.now(), new ArrayList<>(matchedKeywords)));
                }
            } catch (IOException e) {
                System.err.println("Error scraping URL: " + url + " - " + e.getMessage());
            }
        }
        return results;
    }
}
