package com.seven.auth.util;

import com.seven.auth.application.ApplicationDTO;

public class Constants {
    public static final String AUTHORIZATION_DEFAULT_SCHEMA_NAME = "public";
    public static final ApplicationDTO AUTHORIZATION_APPLICATION =
            new ApplicationDTO(null, "authorization", AUTHORIZATION_DEFAULT_SCHEMA_NAME, null, null, null);
}
