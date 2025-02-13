package com.seven.auth.user;


import java.util.Arrays;

public enum UserRole {
    PASSENGER("PASSENGER"),
    OFFICER("OFFICER"),
    ADMIN("ADMIN");
    public String[] privileges;

    UserRole(String roleString) {
            String[] privs = new String[]{"user:c", "user:r", "user:u", "user:d",
                "booking:c", "booking:r", "booking:u", "booking:d",
                "voyage:c", "voyage:r", "voyage:u", "voyage:d",
                "location:c", "location:r", "location:u", "location:d"
            };

            switch (roleString) {
                case "ADMIN": {
                    this.privileges = privs; break;
                }

                case "OFFICER": {
                    this.privileges = privs;break;
                }

                case "PASSENGER": {
                    this.privileges = Arrays.stream(privs).filter((p)-> p.endsWith("r") || p.startsWith("booking")).toArray(String[]::new);
                    break;
                }
            }
    }
}
