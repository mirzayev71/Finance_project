package com.jorabek.finance_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lenderName;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate loanDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false)
    private String status; // "Unpaid" or "Paid"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Default Constructor (JPA uchun zarur)
    public Debt() {
    }

    // Parametrli Constructor
    public Debt(String lenderName, Double amount, LocalDate loanDate, LocalDate returnDate, String status) {
        this.lenderName = lenderName;
        this.amount = amount;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getLenderName() {
        return lenderName;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Debt{" +
                "id=" + id +
                ", lenderName='" + lenderName + '\'' +
                ", amount=" + amount +
                ", loanDate=" + loanDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                '}';
    }
}
