package com.smartblogbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterAIService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model:openai/gpt-3.5-turbo}")
    private String model;

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public String getAISuggestions(String title, String content) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String prompt = "You are an expert blog editor. Analyze the following blog post and provide helpful suggestions for improvement.\n\n"
                    + "### Blog Title: " + title + "\n\n"
                    + "### Blog Content:\n" + content + "\n\n"
                    + "### Task:\n"
                    + "1. Check for grammatical errors or typos.\n"
                    + "2. Identify areas where the flow can be improved.\n"
                    + "3. Suggest more engaging language or alternative phrasing for key sections.\n"
                    + "4. Provide a numbered list of 3-5 specific, actionable suggestions.\n\n"
                    + "### Output Format:\n"
                    + "Provide ONLY the suggestions in a clear, bulleted or numbered list. Do not rewrite the whole post.";

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000"); // Optional for OpenRouter
            headers.set("X-Title", "SmartBlog"); // Optional for OpenRouter

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                OPENROUTER_API_URL, 
                HttpMethod.POST, 
                requestEntity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return message.get("content").toString();
                }
            }

            return "AI did not return any suggestions.";
        } catch (Exception e) {
            System.err.println("Error calling OpenRouter API: " + e.getMessage());
            return "Apologies, AI Suggestion Service is Unavailable at the moment. Please ensure your API key is correctly configured.";
        }
    }
}
