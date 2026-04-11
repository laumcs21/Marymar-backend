package com.marymar.app.UnitTest;

import com.marymar.app.business.Service.ChatbotService;
import com.marymar.app.controller.ChatbotController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerUnitTest {

    @Mock private ChatbotService chatbotService;
    private ChatbotController controller;

    @BeforeEach
    void setUp() { controller = new ChatbotController(chatbotService); }

    @Test
    void responderDeberiaRetornarMapaConRespuesta() {
        when(chatbotService.preguntarIA("Hola")).thenReturn("Hola, ¿en qué te ayudo?");

        Map<String, String> resultado = controller.responder(Map.of("mensaje", "Hola"));

        assertEquals("Hola, ¿en qué te ayudo?", resultado.get("respuesta"));
    }
}
