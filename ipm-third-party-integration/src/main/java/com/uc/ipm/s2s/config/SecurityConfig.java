package com.uc.ipm.s2s.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .cors(Customizer.withDefaults())
          .authorizeHttpRequests(reg -> reg
              .requestMatchers("/actuator/**").permitAll()
              .requestMatchers("/s2s/**").permitAll() // tighten to .authenticated() if needed
              .anyRequest().permitAll()
          );
        return http.build();
    }
}
