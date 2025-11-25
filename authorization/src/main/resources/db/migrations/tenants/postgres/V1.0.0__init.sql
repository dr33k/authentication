CREATE TABLE auth_account (
        id UUID PRIMARY KEY,
        first_name VARCHAR(255) NOT NULL,
        last_name VARCHAR(255)  NOT NULL,
        other_name VARCHAR(255),
        phone_no VARCHAR(20) NOT NULL,
        phone_no_alt VARCHAR(20),
        email VARCHAR(255) NOT NULL UNIQUE,
        email_alt VARCHAR(255) UNIQUE,
        password VARCHAR(512) NOT NULL,
        dob DATE NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
CREATE INDEX auth_account_email_idx ON auth_account(email);

CREATE TABLE auth_role(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE auth_domain(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX auth_domain_name_idx ON auth_domain(name);

CREATE TABLE auth_permission(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    domain_id UUID NOT NULL,
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE
);

CREATE TABLE auth_grant(
    role_id UUID,
    permission_id UUID,
    date_created TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE,
    PRIMARY KEY(role_id, permission_id)
);

CREATE TABLE auth_assignment(
    account_email VARCHAR(255),
    role_id UUID,
    date_created TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    FOREIGN KEY(account_email) REFERENCES auth_account(email) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES auth_role(id) ON DELETE CASCADE,
    PRIMARY KEY(account_email, role_id)
);

DO $$
DECLARE
    root_account_id VARCHAR(255);
    root_account_email VARCHAR(255);
    root_role_id VARCHAR(255);
    root_assignment_id VARCHAR(255);
    system_id VARCHAR(255);
BEGIN
    root_account_id := gen_random_uuid();
    root_account_email := 'root@seven.com';
    root_role_id := 'ROOT';
    root_assignment_id := gen_random_uuid();
    system_id := 'SYSTEM';

-- Create root user
INSERT INTO auth_account(id, name, dob, email, status, date_created, date_updated, created_by, updated_by, password)
VALUES(root_account_id, 'root', '1950-01-01', root_account_email, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id, '$2a$10$rtCR854YRZgX5n2v4MisiubYLHay2Yz/kOJC.9okOfoXc2jjiWYyq');

INSERT INTO auth_role(id, name, description, date_created, date_updated, created_by, updated_by)
VALUES(root_role_id, 'ROOT_ADMIN', 'Root administrator role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, system_id, system_id);

INSERT INTO auth_assignment(id, account_email, role_id, date_created, created_by)
VALUES(root_assignment_id, root_account_email, root_role_id, CURRENT_TIMESTAMP, system_id);

END
$$;
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION insert_domain(
schema_name VARCHAR(255),
d_id VARCHAR(255),
d_name VARCHAR(255),
description VARCHAR(255)
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE FORMAT('
        INSERT INTO %I.auth_domain
        (id, name, description, date_created, date_updated, created_by, updated_by)
        VALUES
        ($1, $2, $3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ''SYSTEM'', ''SYSTEM'');
    ', schema_name)
    USING d_id, d_name, description;
END;
$$;
-----------------------------------------------------------------
CREATE OR REPLACE FUNCTION insert_permission(
schema_name VARCHAR(255),
p_id VARCHAR(255),
p_name VARCHAR(255),
p_type VARCHAR(255),
p_domain_id VARCHAR(255),
description VARCHAR(255)
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE FORMAT('CREATE EXTENSION IF NOT EXISTS "uuid-ossp";');
    EXECUTE FORMAT('
        INSERT INTO %I.auth_permission
        (id, name, type, description, domain_id, date_created, date_updated, created_by, updated_by)
        VALUES
        ($1, $2, $3, $4, $5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ''SYSTEM'', ''SYSTEM'');
     ', schema_name)
    USING p_id, p_name, p_type, description, p_domain_id;
    EXECUTE FORMAT('
        -- Grant to ROOT user
        INSERT INTO %I.auth_grant
        (id, permission_id, role_id, date_created, created_by)
        VALUES
        (gen_random_uuid(), $1, ''ROOT'', CURRENT_TIMESTAMP, ''SYSTEM'');
     ', schema_name)
    USING p_id;
END;
$$;
-----------------------------------------------------------------