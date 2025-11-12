package com.seven.auth.application;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApplicationSearchSpecification {
    public static Specification<Application> getAllAndFilter(ApplicationDTO.Filter applicationFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(applicationFilter.name()))
                criteriaBuilder.like(root.get("name" ),"%"+ applicationFilter.name()+"%");
            if(Objects.nonNull(applicationFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), applicationFilter.dateCreatedFrom()));
            if(Objects.nonNull(applicationFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), applicationFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
