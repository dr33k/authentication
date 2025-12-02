package com.seven.auth;

import com.seven.auth.account.AuthDTO;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.Constants;
import com.seven.auth.util.response.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("su/auth")
@SecurityRequirements
public class JwtSUAuthController {
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public JwtSUAuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> login(@Valid @RequestBody JwtLoginRequest request) throws AuthorizationException {
        AuthDTO userDTO = jwtService.login(request);
        return ok(userDTO.data, userDTO.token);
    }
}
