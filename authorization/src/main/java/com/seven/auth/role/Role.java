package com.seven.auth.role;
import com.seven.auth.config.autdit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_role")
@Data
@ToString
public class Role extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    public Role() {
    }

    public static Role from(RoleDTO.Create req){
        var role = new Role();
        role.setName(req.name());
        role.setDescription(req.description());
        return role;
    }
    public static Role from(RoleDTO.Update req){
        var role = new Role();
        role.setName(req.name());
        role.setDescription(req.description());
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
