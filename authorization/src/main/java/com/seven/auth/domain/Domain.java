package com.seven.auth.domain;
import com.seven.auth.config.audit.AuditableEntity;
import com.seven.auth.permission.Permission;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_domain")
@Data
@ToString
public class Domain extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Permission> permissions;

    public Domain() {
    }

    public Domain(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domain domain = (Domain) o;
        return Objects.equals(id, domain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Domain from(DomainDTO.Create req){
        var d = new Domain();
        d.setName(req.name());
        d.setDescription(req.description());
        d.setPermissions(
                req.permissions().stream().map(Permission::from).toList()
        );
        return d;
    }
    public static Domain from(DomainDTO.Update req){
        var d = new Domain();
        d.setName(req.name());
        d.setDescription(req.description());
        return d;
    }
}
