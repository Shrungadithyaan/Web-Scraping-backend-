package com.example.web_scraping.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapedData {
    private String url;
    private String content;
    private LocalDateTime timestamp;
    private List<String> keywordsFound;
}
