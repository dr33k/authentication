package com.seven.auth.application;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_application")
@Data
@ToString
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "schema_name", nullable = false)
    private String schemaName;

    @Column
    private String description;

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

    public Application() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application application = (Application) o;
        return Objects.equals(id, application.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Application from(ApplicationDTO.Create req){
        var application = new Application();
        application.setName(req.name());
        application.setDescription(req.description());
        application.setSchemaName(req.schemaName());
        return application;
    }
}
