package com.seven.auth;

import com.seven.auth.account.AuthDTO;
import com.seven.auth.util.Constants;
import com.seven.auth.util.response.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
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

    public JwtSUAuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> login(@Valid @RequestBody JwtLoginRequest request){
        AuthDTO userDTO = jwtService.login(request, Constants.AUTHORIZATION_SCHEMA);
        return ok(userDTO.data, userDTO.token);
    }
}
