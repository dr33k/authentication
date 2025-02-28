package com.seven.auth.security.authentication.jwt;

import com.seven.auth.account.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("jwtService")
@ApplicationScope
public class JwtService {
    @Autowired
    Environment environment;
    @Autowired
    AccountService accountService;

    public Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(){
        byte[] bytes = environment.getProperty("jwt.signing.key").getBytes(StandardCharsets.UTF_8);
       return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String subject, Map<String, Object> claims){
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String subject){
        return generateToken(subject, new HashMap<String, Object>());
    }

    public boolean isTokenValid(Claims claims){
        return !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims){
        return claims.getExpiration().before(new Date());
    }

    public UserDTO register(AccountCreateRequest request){
        UserRecord record = accountService.create(request);
        String token = generateToken(record.email(),
                Map.of("role", record.role().name(),
                        "privileges", record.role().privileges));

        return UserDTO.builder().data(record).token(token).build();
    }
    public UserDTO login (Account account){
        String token = generateToken(account.getUsername(),Map.of("role", account.getRole().name(),
                "privileges", account.getRole().privileges));
        return UserDTO.builder().data(UserRecord.copy(account)).token(token).build();
    }
}
