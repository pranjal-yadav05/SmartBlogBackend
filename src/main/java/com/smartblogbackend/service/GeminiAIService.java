package com.smartblogbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    public String generateBlogPost(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEMINI_API_URL + apiKey;

            // ✅ Correct request body format
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            Map<String, Object> responseBody = responseEntity.getBody();

            // ✅ Extract the generated content properly
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return parts.get(0).get("text").toString();
                }
            }

            return "AI did not return content.";
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}

