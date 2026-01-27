package com.jorabek.finance_tracker.repository;

import com.jorabek.finance_tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Barcha tranzaksiyalarni sanaga ko'ra tartiblangan holda olish
    List<Transaction> findAllByOrderByDateDesc();

    // Faqat daromadlarni olish
    @Query("SELECT t FROM Transaction t WHERE t.type = 'Income'")
    List<Transaction> findAllIncome();

    // Faqat xarajatlarni olish
    @Query("SELECT t FROM Transaction t WHERE t.type = 'Expense'")
    List<Transaction> findAllExpense();

    // Umumiy daromadni hisoblash
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'Income'")
    Double calculateTotalIncome();

    // Umumiy xarajatni hisoblash
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'Expense'")
    Double calculateTotalExpense();

    // CHART 1: Xarajatlar kategoriyasi bo'yicha (Pie Chart)
    // Result: [Category, TotalAmount]
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.type = 'Expense' GROUP BY t.category")
    List<Object[]> getExpenseByCategory();

    // CHART 2: Oxirgi 7 kunlik statistika (Bar Chart)
    // Bu murakkabroq, shuning uchun Service-da Java bilan qilamiz yoki native query
    // ishlatamiz.
    // H2 da native query:
    @Query(value = "SELECT t.date, " +
            "SUM(CASE WHEN t.type = 'Income' THEN t.amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN t.type = 'Expense' THEN t.amount ELSE 0 END) as expense " +
            "FROM transactions t " +
            "WHERE t.date >= DATEADD('DAY', -6, CURRENT_DATE) " +
            "GROUP BY t.date ORDER BY t.date", nativeQuery = true)
    List<Object[]> getLast7DaysStats();
}
