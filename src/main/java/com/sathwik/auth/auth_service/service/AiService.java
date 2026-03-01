package com.sathwik.auth.auth_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sathwik.auth.auth_service.dto.OpenRouterRequest;
import com.sathwik.auth.auth_service.dto.OpenRouterResponse;

@Service
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String url;

    private final WebClient webClient;

    public AiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String askLLM(String prompt) {

        OpenRouterRequest req = new OpenRouterRequest(
                "arcee-ai/trinity-mini:free",
                List.of(new OpenRouterRequest.Message("user", prompt))
        );
        System.out.println("API KEY = " + apiKey);


        OpenRouterResponse response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost") // required by OpenRouter
                .header("X-Title", "Todo-AI-App")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OpenRouterResponse.class)
                .block();

        return response.getChoices().get(0).getMessage().getContent();
    }
}

