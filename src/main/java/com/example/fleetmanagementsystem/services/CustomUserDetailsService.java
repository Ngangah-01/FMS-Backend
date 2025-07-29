package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String idNumberStr) throws UsernameNotFoundException {
        logger.debug("Loading user by ID number: {}", idNumberStr);
        try {
            Long idNumber = Long.parseLong(idNumberStr);
            Users user = userRepository.findByidNumber(idNumber)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + idNumberStr));
            logger.debug("User found: {}", user.getIdNumber());
            return new org.springframework.security.core.userdetails.User(
                    String.valueOf(user.getIdNumber()),
//                    user.getIdNumber().toString(),
                    user.getPassword(),
                    user.isEnabled(),
                    true, true, true,

                    user.getRole().stream()
                            .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
            );
        }
        catch (NumberFormatException e){
            throw new UsernameNotFoundException("Invalid ID number format: " + idNumberStr);
        }
    }
}