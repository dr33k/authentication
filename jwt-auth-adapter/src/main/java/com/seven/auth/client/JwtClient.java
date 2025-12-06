package com.seven.auth.client.jwt;

import com.seven.auth.account.AccountDTO;
import com.seven.auth.JwtLoginRequest;
import com.seven.auth.util.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "jwt-client", url = "${AUTHENTICATION_SERVICE_BASE_URL}/auth")
public interface JwtClient {

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<AccountDTO.Record> register(@RequestHeader(value = "X-Tenant-Id") UUID tenantId,
                                         @RequestBody AccountDTO.Create request);

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<AccountDTO.Record> login(@RequestHeader(value = "X-Tenant-Id") UUID tenantId,
                                      @RequestBody JwtLoginRequest request);
}
