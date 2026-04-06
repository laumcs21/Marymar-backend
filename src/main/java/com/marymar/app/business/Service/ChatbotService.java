package com.marymar.app.business.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient;

    // 🧠 Historial en memoria (simple)
    private final List<Map<String, String>> historial = new ArrayList<>();

    public ChatbotService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String preguntarIA(String mensaje) {

        String contexto = """
Eres un asistente virtual del restaurante Mar y Mar.

Tu función es ayudar a los clientes con:
- Horarios de atención
- Menú y productos
- Pedidos
- Información general del restaurante

REGLAS OBLIGATORIAS:
- SOLO puedes saludar una vez al inicio de la conversación
- NO repitas "hola" en mensajes posteriores
- SIEMPRE responde en español
- Sé amable y breve
- NO inventes información

IMPORTANTE:
- Si el cliente quiere reservar:
  Responde EXACTAMENTE:
  "Para realizar una reserva, comunícate al 3003710163"

Información del restaurante:
- Nombre: Mar y Mar
- Tipo: Restaurante de comida de mar
- Ubicación: Colombia
- Horario: 12:00 PM a 5:00 PM
- Comidas destacadas: Arroz marinero, arroz con camarón, ceviches, cazuelas
- Ejecutivos hasta las 2:00 PM
- Solo servicio en mesa
""";

        if (historial.isEmpty()) {
            historial.add(Map.of("role", "system", "content", contexto));
        }

        historial.add(Map.of("role", "user", "content", mensaje));

        Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini",
                "messages", historial
        );

        // 🚀 REQUEST
        String respuesta = webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    List choices = (List) res.get("choices");
                    Map choice = (Map) choices.get(0);
                    Map messageMap = (Map) choice.get("message");
                    return messageMap.get("content").toString();
                })
                .block();

        historial.add(Map.of("role", "assistant", "content", respuesta));

        return respuesta;
    }
}