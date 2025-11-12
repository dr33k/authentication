package com.seven.auth.security.authentication.jwt;

import com.seven.auth.account.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("jwtService")
@ApplicationScope
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    @Autowired
    Environment environment;
    @Autowired
    AccountService accountService;
    @Autowired
    AuthenticationProvider authenticationProvider;

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] bytes = environment.getProperty("jwt.signing.key").getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String subject) {
        return generateToken(subject, new HashMap<String, Object>());
    }

    public boolean isTokenValid(Claims claims) {
        return !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public AuthDTO register(AccountDTO.Create request) {
        try {
            AccountRecord record = accountService.create(request);
            String token = generateToken(record.email(),Map.of()
//                    Map.of("role", record.role().name(),
//                            "privileges", record.role().privileges)
            );

            return AuthDTO.builder().data(record).token(token).build();
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to register account {}. Message: ", request.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to register account {}. Message: ", request.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public AuthDTO login(JwtLoginRequest request) {
        try {
            Account account = (Account) authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())).getPrincipal();
            String token = generateToken(request.getUsername(), Map.of()
//                    Map.of("role", account.getRole().name(),
//                    "privileges", account.getRole().privileges)
            );
            return AuthDTO.builder().data(AccountRecord.copy(account)).token(token).build();

        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to login {}. Message: ", request.getUsername(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to login {}. Message: ", request.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
