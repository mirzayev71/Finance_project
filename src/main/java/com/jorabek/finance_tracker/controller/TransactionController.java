package com.jorabek.finance_tracker.controller;

import com.jorabek.finance_tracker.entity.CategoryLimit;
import com.jorabek.finance_tracker.entity.Debt;
import com.jorabek.finance_tracker.entity.Transaction;
import com.jorabek.finance_tracker.service.DebtService;
import com.jorabek.finance_tracker.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class TransactionController {

    private final TransactionService transactionService;
    private final DebtService debtService;

    @Autowired
    public TransactionController(TransactionService transactionService, DebtService debtService) {
        this.transactionService = transactionService;
        this.debtService = debtService;
    }

    // Asosiy sahifa (Dashboard)
    @GetMapping
    public String index(Model model,
            @RequestParam(value = "sortBy", defaultValue = "date") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        List<Transaction> transactions = transactionService.getAllTransactions(sortBy, direction);
        Double totalIncome = transactionService.getTotalIncome();
        Double totalExpense = transactionService.getTotalExpense();
        Double balance = transactionService.getBalance();

        // Qarzlarni qo'shish
        List<Debt> debts = debtService.getAllDebts();
        Double totalUnpaidDebts = debtService.getTotalUnpaidDebts();

        // 1. CHART DATA
        model.addAttribute("expenseByCategory", transactionService.getExpenseByCategoryData());
        model.addAttribute("last7DaysStats", transactionService.getLast7DaysData());

        // 2. SMART FORMATTING (Formatted Strings)
        model.addAttribute("formattedBalance", transactionService.formatSmart(balance));
        model.addAttribute("formattedIncome", transactionService.formatSmart(totalIncome));
        model.addAttribute("formattedExpense", transactionService.formatSmart(totalExpense));
        model.addAttribute("formattedUnpaidDebts", transactionService.formatSmart(totalUnpaidDebts));

        // 3. FULL FORMATTING (Hover Effect)
        model.addAttribute("fullBalance", transactionService.formatFull(balance));
        model.addAttribute("fullIncome", transactionService.formatFull(totalIncome));
        model.addAttribute("fullExpense", transactionService.formatFull(totalExpense));
        model.addAttribute("fullUnpaidDebts", transactionService.formatFull(totalUnpaidDebts));

        // Asl raqamlar ham kerak bo'lishi mumkin (masalan grafik uchun)
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);

        model.addAttribute("transactions", transactions);

        // Sorting params for UI
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseDirection", direction.equals("asc") ? "desc" : "asc");
        model.addAttribute("transaction", new Transaction());

        // Qarzlar uchun model attributes
        model.addAttribute("debts", debts);
        model.addAttribute("debt", new Debt());

        // 3. BUDGET LIMITS
        model.addAttribute("categoryLimits", transactionService.getAllCategoryLimits());
        model.addAttribute("budgetStatuses", transactionService.getBudgetStatuses());
        model.addAttribute("newLimit", new CategoryLimit());

        return "index";
    }

    // Yangi tranzaksiya qo'shish
    @PostMapping("/add")
    public String addTransaction(@ModelAttribute Transaction transaction) {
        transactionService.saveTransaction(transaction);
        return "redirect:/";
    }

    // Tranzaksiyani o'chirish
    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return "redirect:/";
    }

    // ========== QARZ BOSHQARUVI ==========

    // Yangi qarz qo'shish
    @PostMapping("/debts/add")
    public String addDebt(@ModelAttribute Debt debt) {
        debtService.saveDebt(debt);
        return "redirect:/";
    }

    // Qarzni to'lash (avtomatik xarajat yaratadi)
    @GetMapping("/debts/pay/{id}")
    public String payDebt(@PathVariable Long id) {
        debtService.payDebt(id);
        return "redirect:/";
    }

    // Qarzni o'chirish
    @GetMapping("/debts/delete/{id}")
    public String deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return "redirect:/";
    }

    // ========== SMART FEATURES ==========

    // CSV Export
    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=transactions.csv");

        String csvData = transactionService.generateCsvExport();
        response.getWriter().write(csvData);
    }

    // Limit qo'shish
    @PostMapping("/limits/add")
    public String addLimit(@ModelAttribute CategoryLimit categoryLimit) {
        transactionService.saveCategoryLimit(categoryLimit);
        return "redirect:/";
    }

    // Limitni o'chirish
    @GetMapping("/limits/delete/{id}")
    public String deleteLimit(@PathVariable Long id) {
        transactionService.deleteCategoryLimit(id);
        return "redirect:/";
    }
}
