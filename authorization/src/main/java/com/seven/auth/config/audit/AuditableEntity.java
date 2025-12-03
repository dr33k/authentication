package com.seven.auth.config.audit;

import com.seven.auth.account.Account;

import jakarta.persistence.*;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

/**
 * Base class to hold common auditing fields (createdBy, updatedBy, dateCreated, dateUpdated).
 * Entities extending this class will automatically populate these fields
 * using Spring Data JPA Auditing.
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "email")
    private Account createdBy;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "email")
    private Account updatedBy;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateUpdated;

    protected AuditableEntity() {}
}