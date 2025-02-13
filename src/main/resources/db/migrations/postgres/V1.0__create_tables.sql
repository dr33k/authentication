CREATE TABLE app_user (
        id UUID NOT NULL PRIMARY KEY,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50)  NOT NULL,
        phone_no VARCHAR(15) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(512) NOT NULL,
        dob DATE NOT NULL,
        date_created TIMESTAMP NOT NULL,
        date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        role VARCHAR(20) NOT NULL
);

INSERT INTO r_user(id,dob,date_created,email,first_name,last_name,password,phone_no,role)
VALUES('9ecb86a0-0256-4510-a3d8-c428bff29cde','2000-11-22',current_timestamp,'admin@seven.com','John','Doe','$2a$10$rtCR854YRZgX5n2v4MisiubYLHay2Yz/kOJC.9okOfoXc2jjiWYyq','+2341234567890','ADMIN');