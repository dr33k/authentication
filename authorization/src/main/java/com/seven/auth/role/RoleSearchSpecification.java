package com.seven.auth.role;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoleSearchSpecification {
    public static Specification<Role> getAllAndFilter(RoleDTO.Filter roleFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(roleFilter.name()))
                criteriaBuilder.like(root.get("name" ),"%"+ roleFilter.name()+"%");
            if(Objects.nonNull(roleFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), roleFilter.dateCreatedFrom()));
            if(Objects.nonNull(roleFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), roleFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
