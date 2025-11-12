package com.seven.auth.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DomainSearchSpecification {
    public static Specification<Domain> getAllAndFilter(DomainDTO.Filter domainFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(domainFilter.name()))
                criteriaBuilder.like(root.get("name" ),"%"+ domainFilter.name()+"%");
            if(Objects.nonNull(domainFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), domainFilter.dateCreatedFrom()));
            if(Objects.nonNull(domainFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), domainFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
