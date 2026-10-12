package com.college.placement_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig{
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter){
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // Disable Cross-Site Request Forgery since we are using JWT tokens and not cookies
        http.csrf(csrf -> csrf.disable());

        // Define URL access rules
        http.authorizeHttpRequests(auth -> 
            // Allow anyone to view Swagger UI, Redis tests, and future login/register endpoints
            // .anyRequest().authenticated() requires a valid token for any other type of request
            auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/auth/**", "/error").permitAll().anyRequest().authenticated()
        );

        // Notify Spring if there are no server side sessions (stateless)
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Inject our JWT filter before Spring's default username/password filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // Automatically hash the password using BCrypt algorithm before saving it to MySQL
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        // We need to trigger authentication manager from AuthController later on, so expose it
        return config.getAuthenticationManager();
    }
}