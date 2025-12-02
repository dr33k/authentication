package com.seven.auth.config;

import com.seven.auth.account.AccountDTO;
import com.seven.auth.annotation.AuthorizationHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableJpaAuditing
public class AuthConfiguration implements WebMvcConfigurer {
    private final AuthorizationHandlerInterceptor authorizationHandlerInterceptor;

    public AuthConfiguration(AuthorizationHandlerInterceptor authorizationHandlerInterceptor) {
        this.authorizationHandlerInterceptor = authorizationHandlerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationHandlerInterceptor)
                .excludePathPatterns("/su/**", "/auth/**", "/swagger/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Bean
    @ApplicationScope
    public AccountDTO.Record principal() {
        return (AccountDTO.Record)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
