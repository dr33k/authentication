package com.seven.auth.account;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountSearchSpecification {
    public static Specification<Account> getAllAndFilter(AccountDTO.Filter accountFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(accountFilter.name()))
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("firstName" ),"%"+ accountFilter.name()+"%"),
                        criteriaBuilder.like(root.get("lastName" ),"%"+ accountFilter.name()+"%"),
                        criteriaBuilder.like(root.get("otherName" ),"%"+ accountFilter.name()+"%")
                        )
                );
            if(Objects.nonNull(accountFilter.dateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), accountFilter.dateCreatedFrom()));
            if(Objects.nonNull(accountFilter.dateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), accountFilter.dateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
