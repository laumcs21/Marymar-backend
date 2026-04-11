package com.marymar.app.UnitTest;

import com.marymar.app.configuration.Security.JwtAuthenticationFilter;
import com.marymar.app.configuration.Security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterUnitTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaContinuarSinAutenticacionSiNoHayHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaAutenticarUsuarioSiJwtEsValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-ok");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        UserDetails userDetails = new User("laura@test.com", "hash", java.util.List.of());

        when(jwtService.extractUsername("token-ok")).thenReturn("laura@test.com");
        when(userDetailsService.loadUserByUsername("laura@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-ok", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("laura@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void deberiaContinuarSiJwtEsInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-bad");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("token-bad")).thenThrow(new RuntimeException("jwt invalid"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noDeberiaConsultarUserDetailsSiYaHayAutenticacionEnContexto() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "already-authenticated", null, java.util.List.of())
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-ok");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("token-ok")).thenReturn("laura@test.com");

        filter.doFilter(request, response, chain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldNotFilterDeberiaExcluirRutasPublicas() {
        MockHttpServletRequest authRequest = new MockHttpServletRequest("GET", "/api/auth/login");
        MockHttpServletRequest oauthRequest = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        MockHttpServletRequest errorRequest = new MockHttpServletRequest("GET", "/error");
        MockHttpServletRequest privateRequest = new MockHttpServletRequest("GET", "/api/productos");

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", authRequest));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", oauthRequest));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", errorRequest));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", privateRequest));
    }
}
