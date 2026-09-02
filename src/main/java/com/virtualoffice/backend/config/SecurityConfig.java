package com.virtualoffice.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.virtualoffice.backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableAsync
public class SecurityConfig 
{

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, @Value("${app.allowed.origins}") String allowedOrigins)
    {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = List.of(allowedOrigins.split(","));
    }



    @Bean
    public PasswordEncoder passwordEncoder() 
    {
        return new BCryptPasswordEncoder();
    }


    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception 
    {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {});
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests
        (
            auth -> auth.requestMatchers("/auth/**", "/api/ai/ask", "/ws/**").permitAll().anyRequest().authenticated()
        );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }




    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowCredentials(true);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        /*
            اسمح بأي Header Request
            زي
            headers: 
            {
                "Authorization": "Bearer " + token
            }
            ده هو ال Authorization 
            عملنا * عشان نقول انه هو او لو فيه غيره مستقبلا يبقي مسموح
        
        */  
        configuration.setAllowedHeaders(List.of("*"));

        //اعمل مكان هنربط فيه إعدادات 
        // CORS 
        // بال 
        // Paths.
        UrlBasedCorsConfigurationSource source =new UrlBasedCorsConfigurationSource();

        // طبق الاعدادات ديه عل كل مسار اللي هو يعني 
        // path 
        // ف الباك اند 
        source.registerCorsConfiguration("/**", configuration);

        return source; // يرجعه للسرينج عشان يعرف القواعد
    }
}
