package com.marymar.app.controller;

import com.marymar.app.business.Service.ChatbotService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public Map<String, String> responder(@RequestBody Map<String, String> request) {
        String mensaje = request.get("mensaje");
        String respuesta = chatbotService.preguntarIA(mensaje);
        return Map.of("respuesta", respuesta);
    }
}