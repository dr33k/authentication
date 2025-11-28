package com.seven.auth.permission;
import com.seven.auth.domain.Domain;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_permission")
@Data
public class Permission implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private PermissionType type;

    @JoinColumn(name = "domain_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Domain domain;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateUpdated;

    @Column
    private String createdBy = "SYSTEM";

    @Column
    private String updatedBy = "SYSTEM";

    public Permission() {
    }
    public enum PermissionType{CREATE, READ, UPDATE, DELETE}

    @Override
    public String getAuthority() {
        return name;
    }

    public static Permission from (PermissionDTO.Create req){
        var permission = new Permission();
        permission.setName(req.name());
        permission.setDescription(req.description());
        permission.setType(req.type());
        return permission;
    }

    public static Permission from (PermissionDTO.Update req){
        var permission = new Permission();
        permission.setName(req.name());
        permission.setDescription(req.description());
        permission.setType(req.type());
        return permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission permission = (Permission) o;
        return Objects.equals(id, permission.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
