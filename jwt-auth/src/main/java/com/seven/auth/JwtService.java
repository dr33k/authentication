package com.seven.auth;

import com.seven.auth.account.Account;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AccountService;
import com.seven.auth.account.AuthDTO;
import com.seven.auth.permission.Permission;
import com.seven.auth.permission.PermissionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("jwtService")
@ApplicationScope
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final Environment environment;
    final private AccountService accountService;
    final private PermissionRepository permissionRepository;
    private final AuthenticationProvider authenticationProvider;

    public JwtService(Environment environment, AccountService accountService, PermissionRepository permissionRepository, AuthenticationProvider authenticationProvider) {
        this.environment = environment;
        this.accountService = accountService;
        this.permissionRepository = permissionRepository;
        this.authenticationProvider = authenticationProvider;
    }

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
        ZonedDateTime now = ZonedDateTime.now();
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(now.plusHours(12).toInstant()))
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
            AccountDTO.Record accountRecord = accountService.create(request);
            List<Permission> permissions = permissionRepository.findAllByAccount(accountRecord.email());
            String token = generateToken(accountRecord.email(),
                    Map.of("permissions", permissions,
                            "principal", accountRecord)
            );

            return AuthDTO.builder().data(accountRecord).token(token).build();
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to register account {}. Message: ", request.email(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to register account {}. Message: ", request.email(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public AuthDTO login(JwtLoginRequest request) {
        try {
            Account account = (Account) authenticationProvider
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                    .getPrincipal();
            AccountDTO.Record accountRecord = AccountDTO.Record.from(account);
            List<Permission> permissions = permissionRepository.findAllByAccount(account.getEmail());
            String token = generateToken(account.getEmail(),
                    Map.of("permissions", permissions,
                            "principal", accountRecord)
            );
            return AuthDTO.builder().data(accountRecord).token(token).build();

        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to login {}. Message: ", request.getUsername(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to login {}. Message: ", request.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
