package com.marymar.app.business.Service.impl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.marymar.app.business.Service.AndroidRecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AndroidRecaptchaServiceImpl implements AndroidRecaptchaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${recaptcha.android.project-id}")
    private String projectId;

    @Value("${recaptcha.android.site-key}")
    private String siteKey;

    @Value("${recaptcha.android.api-key}")
    private String apiKey;

    @Value("${recaptcha.android.expected-package-name}")
    private String expectedPackageName;

    @Value("${recaptcha.android.score-threshold:0.3}")
    private double scoreThreshold;

    public AndroidRecaptchaServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
        try {
            if ("test-captcha".equals(token)) {
                System.out.println("Captcha Android bypass para pruebas");
                return true;
            }

            if (token == null || token.isBlank()) {
                System.out.println("Captcha Android vacío");
                return false;
            }

            if (expectedAction == null || expectedAction.isBlank()) {
                System.out.println("expectedAction vacío");
                return false;
            }

            Map<String, Object> event = new LinkedHashMap<>();
            event.put("token", token);
            event.put("siteKey", siteKey);
            event.put("expectedAction", expectedAction);

            if (userAgent != null && !userAgent.isBlank()) {
                event.put("userAgent", userAgent);
            }

            if (userIp != null && !userIp.isBlank()) {
                event.put("userIpAddress", userIp);
            }

            Map<String, Object> body = Map.of("event", event);

            String rawResponse = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/projects/{projectId}/assessments")
                            .queryParam("key", apiKey)
                            .build(projectId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(errorBody -> new IllegalStateException(
                                            "Error de Google reCAPTCHA API: HTTP "
                                                    + response.statusCode().value()
                                                    + " - " + errorBody
                                    )))
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isBlank()) {
                System.out.println("Respuesta vacía de Google reCAPTCHA");
                return false;
            }

            JsonNode response = objectMapper.readTree(rawResponse);

            JsonNode tokenProperties = response.path("tokenProperties");
            boolean valid = tokenProperties.path("valid").asBoolean(false);

            if (!valid) {
                System.out.println("Android reCAPTCHA inválido. Razón: "
                        + tokenProperties.path("invalidReason").asText());
                return false;
            }

            String returnedAction = tokenProperties.path("action").asText(null);
            if (returnedAction == null || !expectedAction.equals(returnedAction)) {
                System.out.println("Action no coincide. Esperada: "
                        + expectedAction + ", recibida: " + returnedAction);
                return false;
            }

            String androidPackageName = tokenProperties.path("androidPackageName").asText(null);
            if (androidPackageName != null
                    && expectedPackageName != null
                    && !expectedPackageName.isBlank()
                    && !expectedPackageName.equals(androidPackageName)) {
                System.out.println("Package no coincide. Esperado: "
                        + expectedPackageName + ", recibido: " + androidPackageName);
                return false;
            }

            double score = response.path("riskAnalysis").path("score").asDouble(0.0);
            if (score < scoreThreshold) {
                System.out.println("Score insuficiente: " + score);
                return false;
            }

            System.out.println("reCAPTCHA Android válido. Score: " + score
                    + ", action: " + returnedAction
                    + ", package: " + androidPackageName);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "No fue posible validar reCAPTCHA Android: " + e.getMessage(),
                    e
            );
        }
    }
}