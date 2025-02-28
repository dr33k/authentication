package com.seven.auth.security.authentication.jwt;

import com.seven.auth.account.AccountService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AccountService accountService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountService accountService) {
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Claims claims = jwtService.extractClaims(token);
                    String username = claims.getSubject();
                    if (username != null) {
                        var user = accountService.loadUserByUsername(username);
                        if (jwtService.isTokenValid(claims)) {
                            request.setAttribute("subject", username);
                            request.setAttribute("role", claims.get("role"));
                            request.setAttribute("privileges", claims.get("privileges"));

                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

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
