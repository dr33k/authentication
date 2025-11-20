package com.seven.auth.assignment;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_assignment")
@Data
@ToString
public class Assignment {
    @EmbeddedId
    private Id id;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    public Assignment() {
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @Embeddable
    public static class Id{
        private String accountEmail;
        private UUID roleId;

        public Id() {
        }

        public Id(String accountEmail, UUID roleId) {
            this.accountEmail = accountEmail;
            this.roleId = roleId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment assignment = (Assignment) o;
        return Objects.equals(id, assignment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Assignment from(AssignmentDTO.Create req){
        Assignment assignment = new Assignment();
        Id id = new Id(req.accountEmail(), req.roleId());
        assignment.setId(id);
        return assignment;
    }
}
