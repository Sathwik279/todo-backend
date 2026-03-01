package com.sathwik.auth.auth_service.service;

<<<<<<< HEAD
import com.sathwik.auth.auth_service.dto.GeminiRequest;
import com.sathwik.auth.auth_service.dto.GeminiResponse;
import com.sathwik.auth.auth_service.repository.TodoRepository;
import jakarta.transaction.Transactional;
=======
import java.util.List;

>>>>>>> f3fd717e0eb0c683a376a02d7c8d849a2c78e534
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

<<<<<<< HEAD
=======
import com.sathwik.auth.auth_service.dto.OpenRouterRequest;
import com.sathwik.auth.auth_service.dto.OpenRouterResponse;

>>>>>>> f3fd717e0eb0c683a376a02d7c8d849a2c78e534
@Service
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

<<<<<<< HEAD
    @Value("${gemini.model}")
    private String model;
=======
    @Value("${gemini.api.url}")
    private String url;
>>>>>>> f3fd717e0eb0c683a376a02d7c8d849a2c78e534

    private final WebClient webClient;
    private final TodoRepository todoRepo;
    private final SseService sseService;

    public AiService(WebClient webClient, TodoRepository todoRepo,SseService sseService) {
        this.webClient = webClient;
        this.todoRepo = todoRepo;
        this.sseService = sseService;
    }

    // 1. THIS is the background worker. It returns void.
    @Async
    public void generateAndSaveAiContent(String todoId, String prompt,String userId) {
        try {
            // Step A: Call the LLM (Takes 3-5 seconds, NO database transaction open!)
            String aiText = askLLM(prompt);

            // Step B: Now that we have the text, open a quick transaction to save it
            todoRepo.findById(todoId).ifPresent(todo -> {
                todo.setAiContent(aiText);
                todoRepo.save(todo);
                sseService.sendAiUpdateToUser(userId, todoId, aiText);
                System.out.println("AI content saved successfully for Todo: " + todoId);
            });

        } catch (Exception e) {
            System.err.println("Background AI task failed: " + e.getMessage());
        }
    }

    public String askLLM(String prompt) {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        GeminiRequest request = new GeminiRequest(prompt);

        try {

            GeminiResponse response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response == null ||
                    response.getCandidates() == null ||
                    response.getCandidates().isEmpty()) {
                return "No AI response";
            }

            return response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI service failed";
        }
    }
}