package com.seven.auth.annotation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class AuthorizationHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        try {
            String tokenSubject = (String) request.getAttribute("subject");
            Set<String> tokenRoles = (Set<String>) request.getAttribute("roles");
            Set<String> tokenPermissions = (Set<String>)request.getAttribute("permissions");

            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                log.info("Java method: {}", handlerMethod.getMethod().getName());
                Authorize authorize = handlerMethod.getMethod().getAnnotation(Authorize.class);

                if (authorize == null) return true;

                Set<String> annRoles = Arrays.stream(authorize.roles()).collect(Collectors.toSet());
                Set<String> annPermissions = Arrays.stream(authorize.privileges()).collect(Collectors.toSet());

                return tokenRoles.stream().anyMatch(annRoles::contains) || tokenPermissions.stream().anyMatch(annPermissions::contains);
            }
            else if (handler instanceof ResourceHttpRequestHandler) return true;

            log.warn("HANDLER INTERCEPTOR LEAK");
            return false;
        }catch (Exception e){throw  new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());}
    }
}
