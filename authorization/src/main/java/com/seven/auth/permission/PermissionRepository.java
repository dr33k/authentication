package com.seven.auth.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {
    Boolean existsByName(String name);

    @Query(value = "SELECT g.permission FROM Grant g JOIN Assignment a ON g.role.id = a.id.roleId WHERE a.id.accountEmail = ?1")
    List<Permission> findAllByAccount(String email);
}
