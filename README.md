# Authentication Service
Schema-per-tenant modular-monolithic authentication solution. 

Plug-and-play adapter for microservices. 

Supports JWT, OAuth2 and Kerberos. 

OAuth2 implementation currently in progress.

## Run JWT application

Java version: 21

Maven version: 3.9.6

Postgres version: 14+

Required db name: auth_db

In project root folder

    $ mvn clean install
    $ mvn -pl jwt-auth spring-boot:run

OR for truly native pseudo-random number generation by BCryptPasswordEncoder on Linux machines
    
    $ mvn clean install
    $ java -Djava.security.egd=file:///dev/random -jar jwt-auth/target/jwt-auth-1.0-SNAPSHOT-exec.jar
