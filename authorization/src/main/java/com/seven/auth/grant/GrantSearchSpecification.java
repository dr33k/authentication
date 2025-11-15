package com.seven.auth.grant;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrantSearchSpecification {
    public static Specification<Grant> getAllAndFilter(GrantDTO.Filter grantFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(Objects.nonNull(grantFilter.permissionId()))
                predicates.add(criteriaBuilder.equal(root.get("permissionId"), grantFilter.permissionId()));
            if(Objects.nonNull(grantFilter.roleId()))
                predicates.add(criteriaBuilder.equal(root.get("roleId"), grantFilter.roleId()));
            if(Objects.nonNull(grantFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), grantFilter.dateCreatedFrom()));
            if(Objects.nonNull(grantFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), grantFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
