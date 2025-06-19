package com.eugene.review_service.repository.specification;

import com.eugene.review_service.model.Rate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RateSpecification {

    private RateSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Rate> findRateByUserAndBook(String userId, String bookId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null && bookId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                predicates.add(criteriaBuilder.equal(root.get("bookId"), bookId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Rate> findRatesByBook(String bookId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) predicates.add(criteriaBuilder.equal(root.get("bookId"), bookId));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
