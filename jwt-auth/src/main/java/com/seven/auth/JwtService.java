package com.seven.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seven.auth.account.Account;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AccountService;
import com.seven.auth.account.AuthDTO;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.permission.PermissionDTO;
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
    final private AccountService accountService;
    final private PermissionRepository permissionRepository;
    private final AuthenticationProvider authenticationProvider;
    private final ObjectMapper objectMapper;

    public JwtService(Environment environment, AccountService accountService, PermissionRepository permissionRepository, AuthenticationProvider authenticationProvider, ObjectMapper objectMapper) {
        this.environment = environment;
        this.accountService = accountService;
        this.permissionRepository = permissionRepository;
        this.authenticationProvider = authenticationProvider;
        this.objectMapper = objectMapper;
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

    public String generateToken(String subject, Map<String, Object> claims) {
        ZonedDateTime now = ZonedDateTime.now();
        return Jwts
                .builder()
                .serializeToJsonWith(new JacksonSerializer <>(objectMapper))
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(now.plusHours(12).toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(Claims claims) {
        return !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public AuthDTO provisionSuper(AccountDTO.Create request) throws AuthorizationException {
        try {
            AccountDTO.Record accountRecord = accountService.create(request);
            List<PermissionDTO.Record> permissionRecords = permissionRepository.findAllByAccount(accountRecord.email()).stream().map(PermissionDTO.Record::from).toList();

            String token = generateToken(accountRecord.email(),
                    Map.of("permissions", permissionRecords,
                            "principal", accountRecord,
                            "tenant", TenantContext.getCurrentTenant())
            );
            return AuthDTO.builder().data(accountRecord).token(token).build();
        } catch (AuthorizationException e) {
            log.error("ResponseStatusException; Unable to register account {}. Message: ", request.email(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to register account {}. Message: ", request.email(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public AuthDTO registerSuper(AccountDTO.Create request) throws AuthorizationException {
        try {
            AccountDTO.Record accountRecord = accountService.createSuper(request);
            List<PermissionDTO.Record> permissionRecords = permissionRepository.findAllByAccount(accountRecord.email()).stream().map(PermissionDTO.Record::from).toList();

            String token = generateToken(accountRecord.email(),
                    Map.of("permissions", permissionRecords,
                            "principal", accountRecord,
                            "tenant", TenantContext.getCurrentTenant())
            );
            return AuthDTO.builder().data(accountRecord).token(token).build();
        } catch (AuthorizationException e) {
            log.error("ResponseStatusException; Unable to register superuser {}. Message: ", request.email(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to register superuser {}. Message: ", request.email(), e);
            throw new ClientException(e.getMessage());
        }
    }

    @Transactional
    public AuthDTO login(JwtLoginRequest request) throws AuthorizationException{
        try {
            String tenant = TenantContext.getCurrentTenant();
            log.info("Login username: {}; tenant: {}", request.getUsername(), tenant);
            Account account = (Account) authenticationProvider
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                    .getPrincipal();
            AccountDTO.Record accountRecord = AccountDTO.Record.from(account);
            List<PermissionDTO.Record> permissionRecords = permissionRepository.findAllByAccount(account.getEmail()).stream().map(PermissionDTO.Record::from).toList();

            String token = generateToken(account.getEmail(),
                    Map.of("permissions", permissionRecords,
                            "principal", accountRecord,
                            "tenant", tenant)
            );
            log.info("User {} logged in successfully", request.getUsername());
            return AuthDTO.builder().data(accountRecord).token(token).build();
        } catch (Exception e) {
            log.error("Unable to login {}. Message: ", request.getUsername(), e);
            throw new ClientException(e.getMessage());
        }
    }
}
