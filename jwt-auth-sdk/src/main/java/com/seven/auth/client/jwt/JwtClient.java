package com.seven.auth.client.jwt;

import com.seven.auth.response.Response;
import com.seven.auth.security.authentication.jwt.JwtLoginRequest;
import com.seven.auth.user.UserCreateRequest;
import com.seven.auth.user.UserRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "jwt-client", url = "${AUTHENTICATION_SERVICE_BASE_URL}/auth/jwt")
public interface JwtClient {

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<UserRecord> register(@RequestBody UserCreateRequest request);

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<UserRecord> login(@RequestBody JwtLoginRequest request);
}
