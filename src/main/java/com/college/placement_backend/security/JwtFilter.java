package com.college.placement_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService){
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        // Search for auth header in incoming request
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Check if header exists and starts with "Bearer" (JWT format)
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            // Skip 7 letters of "Bearer "
            jwt = authHeader.substring(7);
            // Read username from the token
            username = jwtUtil.extractUsername(jwt);
        }

        // Username got found but the user isn't authenticated in the session
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            // Fetch the user from MySQL database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Request JWT to mathematically validate the token
            if(jwtUtil.validateToken(jwt, userDetails.getUsername())){
                // If valid, log the user in Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Let the request continue to the next filter
        filterChain.doFilter(request, response);
    }
}