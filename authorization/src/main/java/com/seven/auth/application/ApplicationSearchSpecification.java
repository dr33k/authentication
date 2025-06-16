package com.seven.auth.application;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApplicationSearchSpecification {
    public static Specification<Application> getAllAndFilter(ApplicationRequest.Filter applicationFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(applicationFilter.getName()))
                criteriaBuilder.like(root.get("name" ),"%"+ applicationFilter.getName()+"%");
            if(Objects.nonNull(applicationFilter.getDateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), applicationFilter.getDateCreatedFrom()));
            if(Objects.nonNull(applicationFilter.getDateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), applicationFilter.getDateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
