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

    @Autowired
    public DebtService(DebtRepository debtRepository, TransactionService transactionService) {
        this.debtRepository = debtRepository;
        this.transactionService = transactionService;
    }

    // Barcha qarzlarni olish
    public List<Debt> getAllDebts() {
        return debtRepository.findAllByOrderByLoanDateDesc();
    }

    // ID bo'yicha qarzni topish
    public Optional<Debt> getDebtById(Long id) {
        return debtRepository.findById(id);
    }

    // Yangi qarz qo'shish
    public Debt saveDebt(Debt debt) {
        // Yangi qarz default holatda "Unpaid"
        if (debt.getStatus() == null || debt.getStatus().isEmpty()) {
            debt.setStatus("Unpaid");
        }
        return debtRepository.save(debt);
    }

    // Qarzni o'chirish
    public void deleteDebt(Long id) {
        debtRepository.deleteById(id);
    }

    // To'lanmagan qarzlarni olish
    public List<Debt> getUnpaidDebts() {
        return debtRepository.findAllUnpaidDebts();
    }

    // To'langan qarzlarni olish
    public List<Debt> getPaidDebts() {
        return debtRepository.findAllPaidDebts();
    }

    // Umumiy to'lanmagan qarzlarni hisoblash
    public Double getTotalUnpaidDebts() {
        Double total = debtRepository.calculateTotalUnpaidDebts();
        return total != null ? total : 0.0;
    }

    // Umumiy to'langan qarzlarni hisoblash
    public Double getTotalPaidDebts() {
        Double total = debtRepository.calculateTotalPaidDebts();
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

            transactionService.saveTransaction(expenseTransaction);
        }
    }
}
