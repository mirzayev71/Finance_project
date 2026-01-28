package com.jorabek.finance_tracker.controller;

import com.jorabek.finance_tracker.entity.CategoryLimit;
import com.jorabek.finance_tracker.entity.Debt;
import com.jorabek.finance_tracker.entity.Transaction;
import com.jorabek.finance_tracker.entity.User;
import com.jorabek.finance_tracker.service.DebtService;
import com.jorabek.finance_tracker.service.TransactionService;
import com.jorabek.finance_tracker.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/")
public class TransactionController {

    private final TransactionService transactionService;
    private final DebtService debtService;
    private final UserService userService; // User ma'lumotlari uchun qo'shildi

    @Autowired
    public TransactionController(TransactionService transactionService, DebtService debtService,
            UserService userService) {
        this.transactionService = transactionService;
        this.debtService = debtService;
        this.userService = userService;
    }

    // Asosiy sahifa (Dashboard)
    @GetMapping
    public String index(Model model, Principal principal,
            @RequestParam(value = "sortBy", defaultValue = "date") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {

        // 1. FOYDALANUVCHINI XAVFSIZ ANIQLASH
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("username", user.getFirstName());
                model.addAttribute("currentUser", user.getUsername());
            } else {
                model.addAttribute("username", principal.getName());
            }
        } else {
            model.addAttribute("username", "Mehmon");
        }

        // 2. MOLIYAVIY MA'LUMOTLAR
        List<Transaction> transactions = transactionService.getAllTransactions(sortBy, direction);
        Double totalIncome = transactionService.getTotalIncome();
        Double totalExpense = transactionService.getTotalExpense();
        Double balance = transactionService.getBalance();

        List<Debt> debts = debtService.getAllDebts();
        Double totalUnpaidDebts = debtService.getTotalUnpaidDebts();

        // 3. CHART VA FORMATTING
        model.addAttribute("expenseByCategory", transactionService.getExpenseByCategoryData());
        model.addAttribute("last7DaysStats", transactionService.getLast7DaysData());

        model.addAttribute("formattedBalance", transactionService.formatSmart(balance));
        model.addAttribute("formattedIncome", transactionService.formatSmart(totalIncome));
        model.addAttribute("formattedExpense", transactionService.formatSmart(totalExpense));
        model.addAttribute("formattedUnpaidDebts", transactionService.formatSmart(totalUnpaidDebts));

        model.addAttribute("fullBalance", transactionService.formatFull(balance));
        model.addAttribute("fullIncome", transactionService.formatFull(totalIncome));
        model.addAttribute("fullExpense", transactionService.formatFull(totalExpense));
        model.addAttribute("fullUnpaidDebts", transactionService.formatFull(totalUnpaidDebts));

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);
        model.addAttribute("transactions", transactions);

        // 4. UI PARAMS
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseDirection", direction.equals("asc") ? "desc" : "asc");
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("debts", debts);
        model.addAttribute("debt", new Debt());

        // 5. BUDGET LIMITS
        model.addAttribute("categoryLimits", transactionService.getAllCategoryLimits());
        model.addAttribute("budgetStatuses", transactionService.getBudgetStatuses());
        model.addAttribute("newLimit", new CategoryLimit());

        return "index";
    }

    @PostMapping("/add")
    public String addTransaction(@ModelAttribute Transaction transaction) {
        transactionService.saveTransaction(transaction);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return "redirect:/";
    }

    @PostMapping("/debts/add")
    public String addDebt(@ModelAttribute Debt debt) {
        debtService.saveDebt(debt);
        return "redirect:/";
    }

    @GetMapping("/debts/pay/{id}")
    public String payDebt(@PathVariable Long id) {
        debtService.payDebt(id);
        return "redirect:/";
    }

    @GetMapping("/debts/delete/{id}")
    public String deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return "redirect:/";
    }

    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=transactions.csv");
        response.getWriter().write(transactionService.generateCsvExport());
    }

    @PostMapping("/limits/add")
    public String addLimit(@ModelAttribute CategoryLimit categoryLimit) {
        transactionService.saveCategoryLimit(categoryLimit);
        return "redirect:/";
    }

    @GetMapping("/limits/delete/{id}")
    public String deleteLimit(@PathVariable Long id) {
        transactionService.deleteCategoryLimit(id);
        return "redirect:/";
    }
}