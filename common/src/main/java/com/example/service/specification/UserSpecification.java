package com.example.service.specification;

import com.example.dto.UserSearchCriteria;
import com.example.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {
    private final UserSearchCriteria criteria;

    public UserSpecification(UserSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public @Nullable Predicate toPredicate(Root<User> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        Path<String> username = root.get("username");
        Path<String> email = root.get("email");
        Path<String> name = root.get("name");

        final List<Predicate> predicates = new ArrayList<Predicate>();

        if(criteria.getName() != null && !criteria.getName().isBlank()) {
            predicates.add(criteriaBuilder.like(name, "%" + criteria.getName() + "%"));
        }

        if(criteria.getUsername() != null && !criteria.getUsername().isBlank()) {
            predicates.add(criteriaBuilder.like(username, "%" + criteria.getUsername() + "%"));
        }

        if(criteria.getEmail() != null && !criteria.getEmail().isBlank()) {
            predicates.add(criteriaBuilder.like(email, "%" + criteria.getEmail() + "%"));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
