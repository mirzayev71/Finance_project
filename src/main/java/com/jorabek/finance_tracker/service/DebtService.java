package com.jorabek.finance_tracker.service;

import com.jorabek.finance_tracker.entity.Debt;
import com.jorabek.finance_tracker.entity.Transaction;
import com.jorabek.finance_tracker.repository.DebtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final TransactionService transactionService;
    private final com.jorabek.finance_tracker.repository.UserRepository userRepository;

    @Autowired
    public DebtService(DebtRepository debtRepository,
            TransactionService transactionService,
            com.jorabek.finance_tracker.repository.UserRepository userRepository) {
        this.debtRepository = debtRepository;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    private com.jorabek.finance_tracker.entity.User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Barcha qarzlarni olish
    public List<Debt> getAllDebts() {
        return debtRepository.findAllByUserOrderByLoanDateDesc(getCurrentUser());
    }

    // ID bo'yicha qarzni topish
    public Optional<Debt> getDebtById(Long id) {
        // Validation needed
        return debtRepository.findById(id);
    }

    // Yangi qarz qo'shish
    public Debt saveDebt(Debt debt) {
        // Yangi qarz default holatda "Unpaid"
        if (debt.getStatus() == null || debt.getStatus().isEmpty()) {
            debt.setStatus("Unpaid");
        }
        debt.setUser(getCurrentUser());
        return debtRepository.save(debt);
    }

    // Qarzni o'chirish
    public void deleteDebt(Long id) {
        debtRepository.deleteById(id);
    }

    // To'lanmagan qarzlarni olish
    public List<Debt> getUnpaidDebts() {
        return debtRepository.findAllUnpaidDebtsByUser(getCurrentUser());
    }

    // To'langan qarzlarni olish
    public List<Debt> getPaidDebts() {
        return debtRepository.findAllPaidDebtsByUser(getCurrentUser());
    }

    // Umumiy to'lanmagan qarzlarni hisoblash
    public Double getTotalUnpaidDebts() {
        Double total = debtRepository.calculateTotalUnpaidDebtsByUser(getCurrentUser());
        return total != null ? total : 0.0;
    }

    // Umumiy to'langan qarzlarni hisoblash
    public Double getTotalPaidDebts() {
        Double total = debtRepository.calculateTotalPaidDebtsByUser(getCurrentUser());
        return total != null ? total : 0.0;
    }

    // Qarzni to'lash - avtomatik xarajat tranzaksiyasi yaratadi
    public void payDebt(Long debtId) {
        Optional<Debt> optionalDebt = debtRepository.findById(debtId);

        if (optionalDebt.isPresent()) {
            Debt debt = optionalDebt.get();

            // 1. Qarz holatini "Paid" ga o'zgartirish
            debt.setStatus("Paid");
            debtRepository.save(debt);

            // 2. Avtomatik xarajat tranzaksiyasi yaratish
            Transaction expenseTransaction = new Transaction();
            expenseTransaction.setDescription("Qarz to'landi: " + debt.getLenderName());
            expenseTransaction.setAmount(debt.getAmount());
            expenseTransaction.setType("Expense");
            expenseTransaction.setCategory("Qarz to'lovi");
            expenseTransaction.setDate(LocalDate.now());

            // transactionService.saveTransaction handles setting user
            transactionService.saveTransaction(expenseTransaction);
        }
    }
}
