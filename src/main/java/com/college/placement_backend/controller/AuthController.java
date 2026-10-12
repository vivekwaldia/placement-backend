package com.college.placement_backend.controller;

import com.college.placement_backend.dto.LoginRequest;
import com.college.placement_backend.dto.SignupRequest;
import com.college.placement_backend.repository.UserRepository;
import com.college.placement_backend.security.JwtUtil;
import com.college.placement_backend.model.Student;
import com.college.placement_backend.model.User;
import com.college.placement_backend.model.Company;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request){
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isPresent()){
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email is already taken");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Instantiate the correct entity on the basis of role
        User newUser;
        if("STUDENT".equalsIgnoreCase(request.getRole())){
            newUser = new Student();
        }
        else if("COMPANY".equalsIgnoreCase(request.getRole())){
            newUser = new Company();
        }
        else{
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid role. Only STUDENT or COMPANY roles allowed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Set details and hash the password
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setRole(request.getRole().toUpperCase());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(newUser);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    // ? = Unknown type
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        try{
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // If the password matches, fetch user details and generate JWT
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            // Return token in a JSON response
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);

            // The below line would return unformatted text, so Postman would receive that unformatted text
            // return ResponseEntity.ok(jwt);
            // We need formatted JSON objects, whenever Spring Boot sees a map is being returned, it converts them
            // into formatted JSON objects before they leave the server

            return ResponseEntity.ok(response);
        }
        // Catch the exception instead of a generic 403 Forbidden
        catch(Exception e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: Bad credentials or user not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}