package com.seven.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
