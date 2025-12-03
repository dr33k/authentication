package com.seven.auth.grant;

import com.seven.auth.config.audit.AuditableEntity;
import com.seven.auth.permission.Permission;
import com.seven.auth.role.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_grant")
@Data
@ToString
public class Grant extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "permission_id")
    @ManyToOne
    private Permission permission;

    @JoinColumn(name = "role_id")
    @ManyToOne
    private Role role;

    @Column
    private String description;

    public Grant() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grant grant = (Grant) o;
        return Objects.equals(id, grant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Grant from(GrantDTO.Create req){
        return new Grant();
    }
}
