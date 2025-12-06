package com.seven.auth.config.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthorizationHandlerInterceptorConfiguration implements WebMvcConfigurer {
    private final AuthorizationHandlerInterceptor authorizationHandlerInterceptor;

    public AuthorizationHandlerInterceptorConfiguration(AuthorizationHandlerInterceptor authorizationHandlerInterceptor) {
        this.authorizationHandlerInterceptor = authorizationHandlerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationHandlerInterceptor)
                .excludePathPatterns("/su/**", "/auth/**", "/swagger/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
