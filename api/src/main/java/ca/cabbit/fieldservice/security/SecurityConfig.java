package ca.cabbit.fieldservice.security;

import ca.cabbit.fieldservice.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.time.Instant;
import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwt, RateLimitFilter rateLimit, ObjectMapper mapper) throws Exception {
        return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; frame-ancestors 'none'")))
                .authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/login","/api/v1/auth/refresh","/actuator/health/**","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll().anyRequest().authenticated())
                .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(res.getOutputStream(), Map.of("code","UNAUTHORIZED","message","Authentication is required","timestamp", Instant.now().toString(),"requestId",Optional.ofNullable(res.getHeader(RequestIdFilter.HEADER)).orElse("")));
                }).accessDeniedHandler((req,res,ex)->{
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN); res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(res.getOutputStream(), Map.of("code","FORBIDDEN","message","You do not have permission","timestamp",Instant.now().toString(),"requestId",Optional.ofNullable(res.getHeader(RequestIdFilter.HEADER)).orElse("")));
                })).addFilterBefore(rateLimit, UsernamePasswordAuthenticationFilter.class).addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(AppProperties p) {
        CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(p.corsOrigins().split(",")).map(String::trim).filter(s->!s.isBlank()).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Request-Id","If-Match"));
        c.setExposedHeaders(List.of("X-Request-Id","ETag")); c.setAllowCredentials(true); c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",c); return source;
    }
}
