package com.seven.auth.security.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class AuthorizationHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        try {
            var jwtSubject = request.getAttribute("subject");
            var jwtRole = request.getAttribute("role");
            ArrayList<String> jwtPrivileges = (ArrayList<String>) (request.getAttribute("privileges"));

            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                log.info("Java method: {}", handlerMethod.getMethod().getName());
                Authorize authorize = handlerMethod.getMethod().getAnnotation(Authorize.class);

                if (authorize == null) return true;

                Set<String> subs = Arrays.stream(authorize.subs()).collect(Collectors.toSet());
                Set<String> roles = Arrays.stream(authorize.roles()).collect(Collectors.toSet());
                Set<String> privileges = Arrays.stream(authorize.privileges()).collect(Collectors.toSet());

                if (subs.contains(jwtSubject) || roles.contains(jwtRole) || jwtPrivileges.stream().anyMatch(privileges::contains))
                    return  true;
                else throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            else if (handler instanceof ResourceHttpRequestHandler) return true;

            log.warn("HANDLER INTERCEPTOR LEAK");
            return false;
        }catch (Exception e){throw  new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());}
    }
}
