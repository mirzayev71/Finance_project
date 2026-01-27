package com.jorabek.finance_tracker.service;

import com.jorabek.finance_tracker.entity.CategoryLimit;
import com.jorabek.finance_tracker.entity.Transaction;
import com.jorabek.finance_tracker.repository.CategoryLimitRepository;
import com.jorabek.finance_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryLimitRepository categoryLimitRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
            CategoryLimitRepository categoryLimitRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryLimitRepository = categoryLimitRepository;
    }

    // Barcha tranzaksiyalarni olish
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByDateDesc();
    }

    // ID bo'yicha tranzaksiyani topish
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // Yangi tranzaksiya qo'shish
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // Tranzaksiyani o'chirish
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    // Umumiy daromadni hisoblash
    public Double getTotalIncome() {
        Double total = transactionRepository.calculateTotalIncome();
        return total != null ? total : 0.0;
    }

    // Umumiy xarajatni hisoblash
    public Double getTotalExpense() {
        Double total = transactionRepository.calculateTotalExpense();
        return total != null ? total : 0.0;
    }

    // Umumiy balansni hisoblash (Daromad - Xarajat)
    public Double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    // Daromad tranzaksiyalarini olish
    public List<Transaction> getIncomeTransactions() {
        return transactionRepository.findAllIncome();
    }

    // Xarajat tranzaksiyalarini olish
    public List<Transaction> getExpenseTransactions() {
        return transactionRepository.findAllExpense();
    }

    // ================== SMART FEATURES ==================

    // 1. SMART FORMATTING
    // 1,000,000 -> "1 mln"
    public String formatSmart(Double amount) {
        if (amount == null)
            return "0.00";

        double abs = Math.abs(amount);

        if (abs >= 1_000_000_000) {
            return String.format("%.1f mlrd", amount / 1_000_000_000);
        } else if (abs >= 1_000_000) {
            return String.format("%.1f mln", amount / 1_000_000);
        } else {
            // 1000 liklarni ajratish (10 000)
            return String.format("%,.2f", amount).replace(",", " ");
        }
    }

    // 2. CHART DATA
    public List<Object[]> getExpenseByCategoryData() {
        return transactionRepository.getExpenseByCategory();
    }

    public List<Object[]> getLast7DaysData() {
        return transactionRepository.getLast7DaysStats();
    }

    // 3. BUDGET LIMITS
    public CategoryLimit saveCategoryLimit(CategoryLimit limit) {
        // Trim category name
        if (limit.getCategory() != null) {
            limit.setCategory(limit.getCategory().trim());
        }

        Optional<CategoryLimit> existing = categoryLimitRepository.findByCategory(limit.getCategory());
        if (existing.isPresent()) {
            CategoryLimit l = existing.get();
            l.setLimitAmount(limit.getLimitAmount());
            return categoryLimitRepository.save(l);
        }
        return categoryLimitRepository.save(limit);
    }

    public List<CategoryLimit> getAllCategoryLimits() {
        return categoryLimitRepository.findAll();
    }

    // Kategoriya bo'yicha limit va ishlatilgan summani olish
    // {Limit, Spent, Percentage}
    public double[] getCategoryBudgetStatus(String category) {
        Optional<CategoryLimit> limitOpt = categoryLimitRepository.findByCategory(category);
        if (limitOpt.isEmpty())
            return null;

        double limit = limitOpt.get().getLimitAmount();

        // Bu kategoriya bo'yicha jami xarajat (hozircha oddiy query bilan)
        // Optimizatsiya: Repository-da yangi metod qo'shish mumkin
        double spent = getAllTransactions().stream()
                .filter(t -> t.getType().equals("Expense") && t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();

        return new double[] { limit, spent, (spent / limit) * 100 };
    }

    // 4. DATA EXPORT (CSV)
    // 1,000,000 -> "1 000 000.00"
    public String formatFull(Double amount) {
        if (amount == null)
            return "0.00";
        return String.format("%,.2f", amount).replace(",", " ");
    }

    public void deleteCategoryLimit(Long id) {
        categoryLimitRepository.deleteById(id);
    }

    public List<com.jorabek.finance_tracker.dto.BudgetStatusDTO> getBudgetStatuses() {
        List<com.jorabek.finance_tracker.dto.BudgetStatusDTO> statuses = new java.util.ArrayList<>();
        List<CategoryLimit> limits = categoryLimitRepository.findAll();

        for (CategoryLimit l : limits) {
            String limitCategory = l.getCategory().trim();
            double spent = getAllTransactions().stream()
                    .filter(t -> t.getType().equals("Expense") &&
                            t.getCategory() != null &&
                            t.getCategory().trim().equalsIgnoreCase(limitCategory))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            statuses.add(
                    new com.jorabek.finance_tracker.dto.BudgetStatusDTO(l.getId(), l.getCategory(), l.getLimitAmount(),
                            spent));
        }
        return statuses;
    }

    public String generateCsvExport() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Description,Type,Category,Amount\n");

        for (Transaction t : getAllTransactions()) {
            csv.append(t.getId()).append(",");
            csv.append(t.getDate()).append(",");
            csv.append("\"").append(t.getDescription().replace("\"", "\"\"")).append("\",");
            csv.append(t.getType()).append(",");
            csv.append(t.getCategory()).append(",");
            csv.append(t.getAmount()).append("\n");
        }
        return csv.toString();
    }
}
