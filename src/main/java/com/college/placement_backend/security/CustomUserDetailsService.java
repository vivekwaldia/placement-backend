package com.college.placement_backend.security;

import com.college.placement_backend.model.User;
import com.college.placement_backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        // Fetch the user entity from MySQL
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new UsernameNotFoundException("User with email " + email + " not found.")
        );

        // Translate User entity into Spring security object ("UserDetails")
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();
    }
}