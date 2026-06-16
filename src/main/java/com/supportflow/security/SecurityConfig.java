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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/frontend/**", "/favicon.ico").permitAll()

                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/users/*/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/agents/*/tickets").hasAnyRole("AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tickets").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/tickets/my").hasRole("USER")

                        .requestMatchers(HttpMethod.GET, "/statistics/overview").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/statistics/me").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/tickets").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/search").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/sla/breached").hasAnyRole("AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tickets/*/attachments").hasAnyRole("USER", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/*/attachments").hasAnyRole("USER", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/attachments/*/download").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/tickets/*").hasAnyRole("USER", "AGENT", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/assign").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/status").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/resolve").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/close").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tickets/*/reopen").hasAnyRole("USER", "AGENT", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://127.0.0.1:5500");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
