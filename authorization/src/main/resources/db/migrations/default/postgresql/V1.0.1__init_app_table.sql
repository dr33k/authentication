SET SCHEMA 'public';

CREATE TABLE auth_application (
        id UUID PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description VARCHAR(255) NOT NULL,
        schema_name VARCHAR(255) NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255) NOT NULL,
        updated_by VARCHAR(255) NOT NULL,
        UNIQUE(name)
);

INSERT INTO auth_application(id, name, description, schema_name, date_created, date_updated, created_by, updated_by)
VALUES (gen_random_uuid(), 'authorization', 'Default authorization application', 'authorization', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'root@seven.com', 'root@seven.com');