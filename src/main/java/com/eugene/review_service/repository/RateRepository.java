package com.eugene.review_service.repository;

import com.eugene.review_service.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RateRepository extends JpaRepository<Rate, Long>, JpaSpecificationExecutor<Rate> {
}
