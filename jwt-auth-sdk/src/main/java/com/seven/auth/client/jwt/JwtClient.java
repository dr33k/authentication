package com.seven.auth.client.jwt;

import com.seven.auth.account.AccountRecord;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.security.authentication.jwt.JwtLoginRequest;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "jwt-client", url = "${AUTHENTICATION_SERVICE_BASE_URL}/auth/jwt")
public interface JwtClient {

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<AccountRecord> register(@RequestBody AccountDTO.Create request);

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<AccountRecord> login(@RequestBody JwtLoginRequest request);
}
