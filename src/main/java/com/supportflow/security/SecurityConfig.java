package com.supportflow.security;

import com.supportflow.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/users/*/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/agents/*/tickets").hasAnyRole("AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tickets").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/tickets/my").hasRole("USER")

                        .requestMatchers(HttpMethod.GET, "/tickets").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/search").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/sla/breached").hasAnyRole("AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/tickets/*").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/assign").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/status").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/resolve").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/close").hasAnyRole("AGENT", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
