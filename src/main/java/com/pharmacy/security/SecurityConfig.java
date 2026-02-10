package com.pharmacy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final String allowedOrigins;
    private final boolean rateLimitEnabled;
    private final int rateLimitWindowSeconds;
    private final int loginLimit;
    private final int registerLimit;
    private final int refreshLimit;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
                          @Value("${app.rate-limit.enabled:true}") boolean rateLimitEnabled,
                          @Value("${app.rate-limit.window-seconds:60}") int rateLimitWindowSeconds,
                          @Value("${app.rate-limit.login-per-window:10}") int loginLimit,
                          @Value("${app.rate-limit.register-per-window:5}") int registerLimit,
                          @Value("${app.rate-limit.refresh-per-window:20}") int refreshLimit) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.allowedOrigins = allowedOrigins;
        this.rateLimitEnabled = rateLimitEnabled;
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
        this.loginLimit = loginLimit;
        this.registerLimit = registerLimit;
        this.refreshLimit = refreshLimit;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(pharmacyCorsConfigurationSource()))
                .csrf(csrf -> csrf.disable())    // disable CSRF
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/api/admin/bootstrap/**",
                                        "/actuator/health",
                                        "/actuator/info",
                                        "/error"
                                ).permitAll()
                                // Role-based access rules
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "CASHIER")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        rateLimitFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource pharmacyCorsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Stream.of(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
        if (origins.contains("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins);
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(
                rateLimitEnabled,
                rateLimitWindowSeconds,
                loginLimit,
                registerLimit,
                refreshLimit
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
