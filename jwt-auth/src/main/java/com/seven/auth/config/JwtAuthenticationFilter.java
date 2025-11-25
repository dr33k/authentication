package com.seven.auth.config;

import com.seven.auth.JwtService;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.permission.Permission;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Claims claims = jwtService.extractClaims(token);
                    String email = claims.getSubject();

                    if (email != null) {
                        if (jwtService.isTokenValid(claims)) {
                            List<Permission> permissions = (List<Permission>) claims.get("permissions");
                            Map<String, Object> accountRecord = (Map<String, Object>) claims.get("principal");
                            request.setAttribute("subject", email);
                            request.setAttribute("permissions", permissions);

                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(accountRecord, null, permissions);

                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                } catch (Exception exception) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
