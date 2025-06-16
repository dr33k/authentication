package com.seven.auth.account;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountSearchSpecification {
    public static Specification<Account> getAllAndFilter(AccountRequest.Filter accountFilter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.isNotBlank(accountFilter.getName()))
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("firstName" ),"%"+ accountFilter.getName()+"%"),
                        criteriaBuilder.like(root.get("lastName" ),"%"+ accountFilter.getName()+"%"),
                        criteriaBuilder.like(root.get("otherName" ),"%"+ accountFilter.getName()+"%")
                        )
                );
            if(Objects.nonNull(accountFilter.getDateCreatedFrom()))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), accountFilter.getDateCreatedFrom()));
            if(Objects.nonNull(accountFilter.getDateCreatedTo()))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), accountFilter.getDateCreatedTo()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
