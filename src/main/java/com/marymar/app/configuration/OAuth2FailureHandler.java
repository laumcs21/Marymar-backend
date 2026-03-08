package com.marymar.app.configuration;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String FRONT_URL;

    @Value("${app.mobile.redirect-uri:marymar://oauth-callback}")
    private String MOBILE_REDIRECT_URI;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        boolean mobileLogin = isMobileLogin(request);
        OAuth2SuccessHandler.clearOAuthClientCookie(response);

        if (mobileLogin) {
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(MOBILE_REDIRECT_URI)
                    .fragment("error=oauth_failed")
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
            return;
        }

        response.sendRedirect(FRONT_URL + "/login?error=oauth_failed");
    }

    private boolean isMobileLogin(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }

        for (Cookie cookie : request.getCookies()) {
            if (OAuth2SuccessHandler.OAUTH_CLIENT_COOKIE.equals(cookie.getName())
                    && OAuth2SuccessHandler.MOBILE_CLIENT.equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }
}