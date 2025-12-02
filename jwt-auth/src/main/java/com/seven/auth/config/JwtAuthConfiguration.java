package com.seven.auth.config;

import com.seven.auth.account.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableMethodSecurity
public class JwtAuthConfiguration{
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;

    public JwtAuthConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, TenantFilter tenantFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers(HttpMethod.POST, "/auth/**", "/su/**").permitAll()
                                .requestMatchers("/swagger", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class)

                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(AccountService accountService) {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider(accountService);
        dao.setUserDetailsPasswordService(accountService);
        dao.setPasswordEncoder(bCryptPasswordEncoder());
        return dao;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @ApplicationScope
    public Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}