package com.jorabek.finance_tracker.repository;

import com.jorabek.finance_tracker.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    // Find all debts by user ordered by loan date
    List<Debt> findAllByUserOrderByLoanDateDesc(com.jorabek.finance_tracker.entity.User user);

    // Find all unpaid debts by user
    @Query("SELECT d FROM Debt d WHERE d.user = :user AND d.status = 'Unpaid'")
    List<Debt> findAllUnpaidDebtsByUser(com.jorabek.finance_tracker.entity.User user);

    // Find all paid debts by user
    @Query("SELECT d FROM Debt d WHERE d.user = :user AND d.status = 'Paid'")
    List<Debt> findAllPaidDebtsByUser(com.jorabek.finance_tracker.entity.User user);

    // Calculate total unpaid debts by user
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Debt d WHERE d.user = :user AND d.status = 'Unpaid'")
    Double calculateTotalUnpaidDebtsByUser(com.jorabek.finance_tracker.entity.User user);

    // Calculate total paid debts by user
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Debt d WHERE d.user = :user AND d.status = 'Paid'")
    Double calculateTotalPaidDebtsByUser(com.jorabek.finance_tracker.entity.User user);
}
