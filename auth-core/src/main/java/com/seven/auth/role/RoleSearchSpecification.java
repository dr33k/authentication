package com.seven.auth.role;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoleSearchSpecification {
    public static Specification<Role> getAllAndFilter(RoleRequest.Filter roleFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(roleFilter.getName()))
                criteriaBuilder.like(root.get("name" ),"%"+ roleFilter.getName()+"%");
            if(Objects.nonNull(roleFilter.getDateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), roleFilter.getDateCreatedFrom()));
            if(Objects.nonNull(roleFilter.getDateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), roleFilter.getDateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
