package com.seven.auth.assignment;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignmentSearchSpecification {
    public static Specification<Assignment> getAllAndFilter(AssignmentDTO.Filter assignmentFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(assignmentFilter.name()))
                criteriaBuilder.like(root.get("name" ),"%"+ assignmentFilter.name()+"%");
            if(Objects.nonNull(assignmentFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), assignmentFilter.dateCreatedFrom()));
            if(Objects.nonNull(assignmentFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), assignmentFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
