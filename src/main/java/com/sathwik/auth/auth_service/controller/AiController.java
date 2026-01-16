package com.sathwik.auth.auth_service.controller;

import com.sathwik.auth.auth_service.dto.AiRequest;
import com.sathwik.auth.auth_service.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody AiRequest req) {

        String answer = aiService.askLLM(req.getPrompt());

        return ResponseEntity.ok(Map.of("reply", answer));
    }
}
