package com.jorabek.finance_tracker.repository;

import com.jorabek.finance_tracker.entity.CategoryLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryLimitRepository extends JpaRepository<CategoryLimit, Long> {
    Optional<CategoryLimit> findByCategoryAndUser(String category, com.jorabek.finance_tracker.entity.User user);

    java.util.List<CategoryLimit> findAllByUser(com.jorabek.finance_tracker.entity.User user);
}
