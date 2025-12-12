package com.seven.auth;

import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AuthDTO;
import com.seven.auth.dto.jwt.JwtLoginRequest;
import com.seven.auth.dto.response.Response;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.Constants;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.seven.auth.dto.response.Responder.created;
import static com.seven.auth.dto.response.Responder.ok;

@RestController
@RequestMapping("/auth/oauth2")
@SecurityRequirements
public class OauthController {
    private final OauthService oauth2Service;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public OauthController(OauthService oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    @PostMapping(value = "/{variant}/{provider}/register", produces = "application/json", consumes = "application/json")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Response> createResource(
            @PathVariable("variant") String variant,
            @PathVariable("provider") String provider,
            @Valid @RequestBody AccountDTO.Create request) throws AuthorizationException {
        log.info("OAuth2 variant {}; provider {}", variant, provider);

//            AuthDTO userDTO = oauth2Service.register(request);
//            return created(userDTO.data, userDTO.token, "/domains" );
        return null;
    }

    @PostMapping(value = "/{variant}/{provider}/login", produces = "application/json", consumes = "application/json")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Response> login(
            @PathVariable("variant") String variant,
            @PathVariable("provider") String provider,
            @Valid @RequestBody JwtLoginRequest request) throws AuthorizationException {
        log.info("OAuth2 variant {}; provider {}", variant, provider);

        //        AuthDTO userDTO = oauth2Service.login(request);
//        return ok(userDTO.data, userDTO.token);
        return null;
    }
}
