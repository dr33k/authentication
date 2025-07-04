package com.seven.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID>, JpaSpecificationExecutor<Domain> {
    Boolean existsByName(String name);
}
