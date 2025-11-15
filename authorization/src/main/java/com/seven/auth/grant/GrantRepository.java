package com.seven.auth.grant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GrantRepository extends JpaRepository<Grant, UUID>, JpaSpecificationExecutor<Grant> {
    Boolean existsByPermissionIdAndRoleId(UUID permissionId, UUID roleId);
}
