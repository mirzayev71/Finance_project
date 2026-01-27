package com.jorabek.finance_tracker.repository;

import com.jorabek.finance_tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions by user ordered by date
    List<Transaction> findAllByUserOrderByDateDesc(com.jorabek.finance_tracker.entity.User user);

    // Find all income by user
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.type = 'Income'")
    List<Transaction> findAllIncomeByUser(com.jorabek.finance_tracker.entity.User user);

    // Find all expense by user
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.type = 'Expense'")
    List<Transaction> findAllExpenseByUser(com.jorabek.finance_tracker.entity.User user);

    // Calculate total income by user
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user = :user AND t.type = 'Income'")
    Double calculateTotalIncomeByUser(com.jorabek.finance_tracker.entity.User user);

    // Calculate total expense by user
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user = :user AND t.type = 'Expense'")
    Double calculateTotalExpenseByUser(com.jorabek.finance_tracker.entity.User user);

    // Expenses by category for user
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'Expense' GROUP BY t.category")
    List<Object[]> getExpenseByCategoryByUser(com.jorabek.finance_tracker.entity.User user);

    // Last 7 days stats (JPQL - DB Agnostic)
    @Query("SELECT t.date, " +
            "SUM(CASE WHEN t.type = 'Income' THEN t.amount ELSE 0.0 END), " +
            "SUM(CASE WHEN t.type = 'Expense' THEN t.amount ELSE 0.0 END) " +
            "FROM Transaction t " +
            "WHERE t.user = :user AND t.date >= :startDate " +
            "GROUP BY t.date ORDER BY t.date")
    List<Object[]> getLast7DaysStatsByUser(com.jorabek.finance_tracker.entity.User user, java.time.LocalDate startDate);

    // For sorting
    List<Transaction> findAllByUser(com.jorabek.finance_tracker.entity.User user,
            org.springframework.data.domain.Sort sort);
}
