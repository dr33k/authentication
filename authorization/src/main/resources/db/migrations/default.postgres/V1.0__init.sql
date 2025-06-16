CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE auth_application (
        id UUID PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        schema_name VARCHAR(255) NOT NULL,
        description VARCHAR(255) NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(name)
);