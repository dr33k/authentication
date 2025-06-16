package com.seven.auth.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DomainSearchSpecification {
    public static Specification<Domain> getAllAndFilter(DomainRequest.Filter domainFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(domainFilter.getName()))
                criteriaBuilder.like(root.get("name" ),"%"+ domainFilter.getName()+"%");
            if(Objects.nonNull(domainFilter.getDateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), domainFilter.getDateCreatedFrom()));
            if(Objects.nonNull(domainFilter.getDateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), domainFilter.getDateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
