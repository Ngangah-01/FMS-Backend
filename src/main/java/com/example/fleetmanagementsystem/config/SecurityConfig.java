package com.example.fleetmanagementsystem.config;


import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private  String jwtSecret;
    public SecurityConfig(JwtUtil jwtUtil,
                          UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Make authentication stateless
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/error").permitAll() // Allow login access
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/matatus").hasAnyRole("ADMIN","MARSHALL","DRIVER","CONDUCTOR")
                        .requestMatchers("/api/matatus/**").authenticated() // Require authentication, role checked by @PreAuthorize
                        .requestMatchers("/api/routes").hasAnyRole("ADMIN","MARSHALL")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT authentication filter;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }


//    @Bean
//    public UserDetailsService userDetailsService() {
//        return username -> {
//            Users user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)); // already handled by jwtAuthenticationFilter
//            return User.withUsername(user.getUsername())
//                    .password(user.getPassword())
//                    .role(user.getRole().toArray(new String[0]))
//                    .build();
//        };
//    }
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
//        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
//        return NimbusJwtDecoder.withSecretKey(secretKey).build();
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            String role = jwt.getClaimAsString("role");
//            return Arrays.stream(role.split(","))
//                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                    .collect(Collectors.toList());
//        });
//        return converter;
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
