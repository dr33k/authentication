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

CREATE TABLE auth_domain(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_grant(
    role_id UUID,
    permission_id UUID,
    domain_id UUID,
    date_created TIMESTAMP NOT NULL,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE,
    PRIMARY KEY(role_id, permission_id, domain_id)
);

CREATE TABLE auth_assignment(
    account_email VARCHAR(255),
    role_id UUID,
    date_created TIMESTAMP NOT NULL,
    FOREIGN KEY(account_email) REFERENCES auth_account(email) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    PRIMARY KEY(account_email, role_id)
);
CREATE INDEX ON auth_assignment(account_email);

INSERT INTO auth_permission(id, name, description, date_created, date_updated)
VALUES
(random_uuid_gen(), 'READ', 'Permission to perform a GET operation on resources', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(random_uuid_gen(), 'CREATE', 'Permission to perform a POST operation on resources', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(random_uuid_gen(), 'UPDATE', 'Permission to perform a PUT operation on resources', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(random_uuid_gen(), 'DELETE', 'Permission to perform a DELETE operation on resources', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO account(id,dob,date_created,email,first_name,last_name,password,phone_no,role)
VALUES('9ecb86a0-0256-4510-a3d8-c428bff29cde','2000-11-22',current_timestamp,'admin@seven.com','John','Doe','$2a$10$rtCR854YRZgX5n2v4MisiubYLHay2Yz/kOJC.9okOfoXc2jjiWYyq','+2341234567890','ADMIN');