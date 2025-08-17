package com.eugene.review_service.repository;

import com.eugene.review_service.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RateRepository
        extends JpaRepository<Rate, Long>, JpaSpecificationExecutor<Rate>
{
    Optional<Rate> findByUserIdAndBookId(
            String userId,
            String bookId
    );
    
    List<Rate> findAllByBookId(String bookId);
}
