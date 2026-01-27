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
    private final com.jorabek.finance_tracker.repository.UserRepository userRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
            CategoryLimitRepository categoryLimitRepository,
            com.jorabek.finance_tracker.repository.UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryLimitRepository = categoryLimitRepository;
        this.userRepository = userRepository;
    }

    private com.jorabek.finance_tracker.entity.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Barcha tranzaksiyalarni olish (User va Sorting bilan)
    public List<Transaction> getAllTransactions(String sortBy, String direction) {
        com.jorabek.finance_tracker.entity.User user = getCurrentUser();
        if (sortBy == null || sortBy.isEmpty()) {
            return transactionRepository.findAllByUserOrderByDateDesc(user);
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(sortBy);
        if ("asc".equalsIgnoreCase(direction)) {
            sort = sort.ascending();
        } else {
            sort = sort.descending();
        }

        return transactionRepository.findAllByUser(user, sort);
    }

    // Overload for internal use (keeps default behavior)
    public List<Transaction> getAllTransactions() {
        return getAllTransactions("date", "desc");
    }

    // ID bo'yicha tranzaksiyani topish
    public Optional<Transaction> getTransactionById(Long id) {
        // Security check could be added here to ensure transaction belongs to user
        return transactionRepository.findById(id);
    }

    // Yangi tranzaksiya qo'shish
    public Transaction saveTransaction(Transaction transaction) {
        transaction.setUser(getCurrentUser());
        return transactionRepository.save(transaction);
    }

    // Tranzaksiyani o'chirish
    public void deleteTransaction(Long id) {
        // Should ideally check ownership
        transactionRepository.deleteById(id);
    }

    // Umumiy daromadni hisoblash
    public Double getTotalIncome() {
        Double total = transactionRepository.calculateTotalIncomeByUser(getCurrentUser());
        return total != null ? total : 0.0;
    }

    // Umumiy xarajatni hisoblash
    public Double getTotalExpense() {
        Double total = transactionRepository.calculateTotalExpenseByUser(getCurrentUser());
        return total != null ? total : 0.0;
    }

    // Umumiy balansni hisoblash (Daromad - Xarajat)
    public Double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    // Daromad tranzaksiyalarini olish
    public List<Transaction> getIncomeTransactions() {
        return transactionRepository.findAllIncomeByUser(getCurrentUser());
    }

    // Xarajat tranzaksiyalarini olish
    public List<Transaction> getExpenseTransactions() {
        return transactionRepository.findAllExpenseByUser(getCurrentUser());
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
        return transactionRepository.getExpenseByCategoryByUser(getCurrentUser());
    }

    public List<Object[]> getLast7DaysData() {
        com.jorabek.finance_tracker.entity.User user = getCurrentUser();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = today.minusDays(6);

        // DB dan ma'lumotni olamiz
        List<Object[]> dbData = transactionRepository.getLast7DaysStatsByUser(user, startDate);

        // Map ga o'tkazamiz oson ishlash uchun: Date -> {Income, Expense}
        java.util.Map<String, double[]> dataMap = new java.util.HashMap<>();
        for (Object[] row : dbData) {
            // row[0] is usually sql.Date or String depending on DB, safely toString()
            String dateStr = row[0].toString();
            double income = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            double expense = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            dataMap.put(dateStr, new double[] { income, expense });
        }

        // 7 kunlik to'liq ro'yxatni shakllantiramiz
        List<Object[]> result = new java.util.ArrayList<>();
        // Simple "dd.MM" format for the chart label (e.g. "27.01")
        java.time.format.DateTimeFormatter labelDtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM");
        // DB key format (must match what DB returns, assuming YYYY-MM-DD standard)
        java.time.format.DateTimeFormatter dbKeyDtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < 7; i++) {
            java.time.LocalDate date = startDate.plusDays(i);
            String dbKey = date.format(dbKeyDtf);
            String labelKey = date.format(labelDtf);

            double[] values = dataMap.getOrDefault(dbKey, new double[] { 0.0, 0.0 });
            // Return: Label String, Income, Expense
            result.add(new Object[] { labelKey, values[0], values[1] });
        }

        return result;
    }

    // 3. BUDGET LIMITS
    public CategoryLimit saveCategoryLimit(CategoryLimit limit) {
        // Trim category name
        if (limit.getCategory() != null) {
            limit.setCategory(limit.getCategory().trim());
        }

        // Note: CategoryLimit is not yet user-isolated in DB schema,
        // so this will be global. (Simplification based on instructions)
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

        // Bu kategoriya bo'yicha jami xarajat (user isolated via getAllTransactions)
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
            // getAllTransactions is already filtered by User
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
