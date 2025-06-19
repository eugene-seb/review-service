package com.eugene.review_service.repository.specification;

import com.eugene.review_service.model.Comment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CommentSpecification {

    private CommentSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Comment> findCommentsByBook(String bookId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) predicates.add(criteriaBuilder.equal(root.get("bookId"), bookId));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
