package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.RecaptchaService;
import com.marymar.app.business.DTO.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${recaptcha.secret}")
    private String secret;

    private static final String VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    public boolean validarCaptcha(String token) {

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", secret);
        requestBody.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(requestBody, headers);

        RecaptchaResponse response = restTemplate.postForObject(
                VERIFY_URL,
                request,
                RecaptchaResponse.class
        );

        System.out.println("TOKEN QUE LLEGA: " + token);
        System.out.println("SECRET USADO: " + secret);

        if (response != null) {
            System.out.println("SUCCESS: " + response.isSuccess());
            System.out.println("ERRORS: " + response.getErrorCodes());
            System.out.println("HOSTNAME: " + response.getHostname());
        }

        return response != null && response.isSuccess();
    }
}
