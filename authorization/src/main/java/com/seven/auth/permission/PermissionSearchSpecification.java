package com.seven.auth.permission;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermissionSearchSpecification {
    public static Specification<Permission> getAllAndFilter(PermissionDTO.Filter permissionFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(permissionFilter.name()))
                criteriaBuilder.like(root.get("name" ),"%"+ permissionFilter.name()+"%");
            if(Objects.nonNull(permissionFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), permissionFilter.dateCreatedFrom()));
            if(Objects.nonNull(permissionFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), permissionFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
