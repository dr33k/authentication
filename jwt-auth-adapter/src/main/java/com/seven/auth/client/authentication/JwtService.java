package com.seven.auth.client.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seven.auth.JwtLoginRequest;
import com.seven.auth.account.Account;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AccountService;
import com.seven.auth.account.AuthDTO;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.permission.Permission;
import com.seven.auth.permission.PermissionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.ApplicationScope;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("jwtService")
@ApplicationScope
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final Environment environment;

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] bytes = Objects.requireNonNull(environment.getProperty("jwt.signing-key")).getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public boolean isTokenValid(Claims claims) {
        return !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

}
