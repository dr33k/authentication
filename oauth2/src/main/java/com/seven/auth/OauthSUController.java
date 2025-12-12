package com.seven.auth;

import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AuthDTO;
import com.seven.auth.dto.jwt.JwtLoginRequest;
import com.seven.auth.dto.response.Response;
import com.seven.auth.exception.AuthorizationException;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.seven.auth.dto.response.Responder.ok;

@RestController
@RequestMapping("su/auth/oauth2")
public class OauthSUController {
    private final OauthService oauth2Service;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public OauthSUController(OauthService oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    @SecurityRequirements
    @PostMapping(value = "/{variant}/{provider}/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> login(
            @PathVariable("variant") String variant,
            @PathVariable("provider") String provider,
            @Valid @RequestBody JwtLoginRequest request) throws AuthorizationException {
        log.info("OAuth2 variant {}; provider {}", variant, provider);
//        AuthDTO userDTO = oauth2Service.login(request);
//        return ok(userDTO.data, userDTO.token);
        return null;
    }

    @PostMapping(value = "/{variant}/{provider}/provision", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> provisionSuper(
            @PathVariable("variant") String variant,
            @PathVariable("provider") String provider,
            @Valid @RequestBody AccountDTO.Create request) throws AuthorizationException {
        log.info("OAuth2 variant {}; provider {}", variant, provider);

//        AuthDTO userDTO = oauth2Service.registerSuper(request);
//        return ok(userDTO.data, userDTO.token);
        return null;
    }
}
