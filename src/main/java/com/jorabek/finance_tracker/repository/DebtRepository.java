package com.jorabek.finance_tracker.repository;

import com.jorabek.finance_tracker.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    // Barcha qarzlarni sanaga ko'ra tartiblangan holda olish
    List<Debt> findAllByOrderByLoanDateDesc();

    // Faqat to'lanmagan qarzlarni olish
    @Query("SELECT d FROM Debt d WHERE d.status = 'Unpaid'")
    List<Debt> findAllUnpaidDebts();

    // Faqat to'langan qarzlarni olish
    @Query("SELECT d FROM Debt d WHERE d.status = 'Paid'")
    List<Debt> findAllPaidDebts();

    // Umumiy to'lanmagan qarzlarni hisoblash
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Debt d WHERE d.status = 'Unpaid'")
    Double calculateTotalUnpaidDebts();

    // Umumiy to'langan qarzlarni hisoblash
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Debt d WHERE d.status = 'Paid'")
    Double calculateTotalPaidDebts();
}
