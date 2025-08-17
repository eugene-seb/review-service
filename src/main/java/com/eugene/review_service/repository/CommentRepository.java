package com.eugene.review_service.repository;

import com.eugene.review_service.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment>
{
    List<Comment> findAllByBookId(String bookId);
}
