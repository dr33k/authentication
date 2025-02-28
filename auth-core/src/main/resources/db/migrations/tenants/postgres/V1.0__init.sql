CREATE TABLE auth_account (
        id UUID PRIMARY KEY,
        first_name VARCHAR(255) NOT NULL,
        last_name VARCHAR(255)  NOT NULL,
        other_name VARCHAR(255),
        phone_no VARCHAR(20) NOT NULL,
        phone_no_alt VARCHAR(20),
        email VARCHAR(255) NOT NULL UNIQUE,
        email_alt VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(512) NOT NULL,
        dob DATE NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

CREATE TABLE auth_role(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_permission(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_grant(
    role_id UUID PRIMARY KEY,
    permission_id UUID PRIMARY KEY,
    date_created TIMESTAMP NOT NULL,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE
);

CREATE TABLE auth_assignment(
    account_id UUID PRIMARY KEY,
    role_id UUID PRIMARY KEY,
    date_created TIMESTAMP NOT NULL,
    FOREIGN KEY(account_id) REFERENCES auth_account(id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE
);

INSERT INTO account(id,dob,date_created,email,first_name,last_name,password,phone_no,role)
VALUES('9ecb86a0-0256-4510-a3d8-c428bff29cde','2000-11-22',current_timestamp,'admin@seven.com','John','Doe','$2a$10$rtCR854YRZgX5n2v4MisiubYLHay2Yz/kOJC.9okOfoXc2jjiWYyq','+2341234567890','ADMIN');