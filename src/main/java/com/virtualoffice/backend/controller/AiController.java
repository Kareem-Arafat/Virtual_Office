package com.virtualoffice.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.AiRequestDTO;
import com.virtualoffice.backend.dto.AiResponseDTO;
import com.virtualoffice.backend.service.AiService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController
{
    private final AiService aiService;

    public AiController(AiService aiService)
    {
        this.aiService = aiService;
    }


    @PostMapping("/ask")
    public AiResponseDTO askAi(@Valid @RequestBody AiRequestDTO request, Authentication authentication)
    {
        String username = null;

        if(authentication != null)
        {
            username = authentication.getName();
        }

        String response = aiService.getAiResponse(request.getPrompt(), username);

        return new AiResponseDTO(response);
    }
}
