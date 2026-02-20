package com.marymar.app.configuration;

import com.marymar.app.configuration.Security.JwtAuthenticationFilter;
import com.marymar.app.configuration.Security.JwtService;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PersonaRepository personaRepository;

    public SecurityConfig(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        return new DaoAuthenticationProvider(userDetailsService);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username ->
                personaRepository.findByEmail(username)
                        .map(persona ->
                                new User(
                                        persona.getEmail(),
                                        persona.getContrasena(),
                                        Collections.singleton(
                                                new SimpleGrantedAuthority("ROLE_" + persona.getRol().name())
                                        )
                                )
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException("No existe cuenta con email: " + username)
                        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider(userDetailsService(), passwordEncoder()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/validate-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-token").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().permitAll()                )

                //.oauth2Login(oauth -> oauth
                //        .successHandler(successHandler)
                //)

                //.oauth2Client(oauth -> {})

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

