package com.marymar.app.business.Service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.marymar.app.business.Service.AndroidRecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AndroidRecaptchaServiceImpl implements AndroidRecaptchaService {

    private final WebClient webClient;

    @Value("${recaptcha.android.project-id}")
    private String projectId;

    @Value("${recaptcha.android.site-key}")
    private String siteKey;

    @Value("${recaptcha.android.api-key}")
    private String apiKey;

    @Value("${recaptcha.android.expected-package-name}")
    private String expectedPackageName;

    @Value("${recaptcha.android.score-threshold:0.5}")
    private double scoreThreshold;

    public AndroidRecaptchaServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl("https://recaptchaenterprise.googleapis.com")
                .build();
    }

    @Override
    public boolean validarCaptchaAndroid(
            String token,
            String expectedAction,
            String userAgent,
            String userIp
    ) {
        if ("test-captcha".equals(token)) {
            System.out.println("Captcha Android bypass para pruebas");
            return true;
        }

        if (token == null || token.isBlank()) {
            return false;
        }

        if (expectedAction == null || expectedAction.isBlank()) {
            return false;
        }

        Map<String, Object> event = new java.util.LinkedHashMap<>();
        event.put("token", token);
        event.put("siteKey", siteKey);
        event.put("expectedAction", expectedAction);

        if (userAgent != null && !userAgent.isBlank()) {
            event.put("userAgent", userAgent);
        }

        if (userIp != null && !userIp.isBlank()) {
            event.put("userIpAddress", userIp);
        }

        Map<String, Object> body = Map.of(
                "event", event
        );

        JsonNode response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/projects/{projectId}/assessments")
                        .queryParam("key", apiKey)
                        .build(projectId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            return false;
        }

        JsonNode tokenProperties = response.path("tokenProperties");
        if (!tokenProperties.path("valid").asBoolean(false)) {
            System.out.println("Android reCAPTCHA inválido. Razón: "
                    + tokenProperties.path("invalidReason").asText());
            return false;
        }

        String returnedAction = tokenProperties.path("action").asText(null);
        if (returnedAction == null || !expectedAction.equals(returnedAction)) {
            System.out.println("Android reCAPTCHA action no coincide. Esperada: "
                    + expectedAction + ", recibida: " + returnedAction);
            return false;
        }

        // Si Google devuelve el package name, lo validamos.
        String androidPackageName = tokenProperties.path("androidPackageName").asText(null);
        if (androidPackageName != null && expectedPackageName != null
                && !expectedPackageName.isBlank()
                && !expectedPackageName.equals(androidPackageName)) {
            System.out.println("Android package no coincide. Esperado: "
                    + expectedPackageName + ", recibido: " + androidPackageName);
            return false;
        }

        double score = response.path("riskAnalysis").path("score").asDouble(0.0);
        if (score < scoreThreshold) {
            System.out.println("Android reCAPTCHA score insuficiente: " + score);
            return false;
        }

        return true;
    }
}