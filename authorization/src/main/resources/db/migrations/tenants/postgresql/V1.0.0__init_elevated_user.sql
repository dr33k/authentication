CREATE TABLE auth_account (
        id UUID PRIMARY KEY,
        first_name VARCHAR(255) NOT NULL,
        last_name VARCHAR(255)  NOT NULL,
        other_name VARCHAR(255),
        phone_no VARCHAR(20) NOT NULL,
        phone_no_alt VARCHAR(20),
        email VARCHAR(255) NOT NULL UNIQUE,
        email_alt VARCHAR(255) UNIQUE,
        status VARCHAR(20) NOT NULL,
        password VARCHAR(512) NOT NULL,
        dob DATE NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255)
        );
CREATE INDEX auth_account_email_idx ON auth_account(email);

CREATE TABLE auth_role(
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE auth_domain(
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX auth_domain_name_idx ON auth_domain(name);

CREATE TABLE auth_permission(
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    type VARCHAR(20) NOT NULL,
    domain_id UUID NOT NULL,
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY(domain_id) REFERENCES auth_domain(id) ON DELETE CASCADE
);

CREATE TABLE auth_grant(
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE
);

CREATE TABLE auth_assignment(
    account_email VARCHAR(255) NOT NULL,
    role_id UUID NOT NULL,
    date_created TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    FOREIGN KEY(account_email) REFERENCES auth_account(email) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    PRIMARY KEY(account_email, role_id)
);

DO $$
DECLARE
    admin_account_id UUID;
    admin_account_email VARCHAR(255);
    admin_role_id UUID;
    system_id VARCHAR(255);
    global_domain_id UUID;
    elev_create_id UUID;
    elev_update_id UUID;
    elev_read_id UUID;
    elev_delete_id UUID;
BEGIN
    admin_account_id := gen_random_uuid();
    admin_account_email := current_schema||'@seven.com';
    admin_role_id := gen_random_uuid();
    system_id := 'SYSTEM';
    global_domain_id := gen_random_uuid();
    elev_create_id = gen_random_uuid();
    elev_update_id = gen_random_uuid();
    elev_read_id = gen_random_uuid();
    elev_delete_id = gen_random_uuid();

-- Create an elevated user
INSERT INTO auth_account(id, first_name, last_name, dob, email, phone_no, status, date_created, date_updated, created_by, updated_by, password)
VALUES(admin_account_id, 'admin', '', '1950-01-01', admin_account_email, '+2349999999990', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id, '$2a$12$7BtwA4ZTgyVGM2F7SiCZaeAsM4VD1eP52zrSEdkaP3S60IxCgaXIC');

INSERT INTO auth_role(id, name, description, date_created, date_updated, created_by, updated_by)
VALUES(admin_role_id, 'ADMIN', 'Administrator role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id);

INSERT INTO auth_assignment(account_email, role_id, date_created, created_by)
VALUES(admin_account_email, admin_role_id, CURRENT_TIMESTAMP, system_id);

INSERT INTO auth_domain(id, name, description, date_created, date_updated, created_by, updated_by)
VALUES(global_domain_id, current_schema, 'This is an umbrella domain for all the future domains in the '||current_schema||' schema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id);

INSERT INTO auth_permission(id, name, description, type, domain_id, date_created, date_updated, created_by, updated_by)
VALUES
(elev_create_id, 'elev_create_'||current_schema, 'This represents the CREATE permission that overrides all others for the '||current_schema||' domain', 'CREATE', global_domain_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id),
(elev_read_id, 'elev_read_'||current_schema, 'This represents the READ permission that overrides all others for the '||current_schema||' domain', 'READ', global_domain_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id),
(elev_update_id, 'elev_update_'||current_schema, 'This represents the UPDATE permission that overrides all others for the '||current_schema||' domain', 'UPDATE', global_domain_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id),
(elev_delete_id, 'elev_delete_'||current_schema, 'This represents the DELETE permission that overrides all others for the '||current_schema||' domain', 'DELETE', global_domain_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id)
;

INSERT INTO auth_grant(id, role_id, permission_id, description, date_created, created_by)
VALUES
(gen_random_uuid(), admin_role_id, elev_create_id, 'Grants the elev_create role to the admin', CURRENT_TIMESTAMP, system_id),
(gen_random_uuid(), admin_role_id, elev_update_id, 'Grants the elev_update role to the admin', CURRENT_TIMESTAMP, system_id),
(gen_random_uuid(), admin_role_id, elev_read_id, 'Grants the elev_read role to the admin', CURRENT_TIMESTAMP, system_id),
(gen_random_uuid(), admin_role_id, elev_delete_id, 'Grants the elev_delete role to the admin', CURRENT_TIMESTAMP, system_id)
;
END
$$;