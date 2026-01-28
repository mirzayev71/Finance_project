package com.jorabek.finance_tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_limits", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "category" })
})
public class CategoryLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double limitAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Default Constructor
    public CategoryLimit() {
    }

    // Parameterized Constructor
    public CategoryLimit(String category, Double limitAmount, User user) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.user = user;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getLimitAmount() {
        return limitAmount;
    }

    public User getUser() {
        return user;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLimitAmount(Double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
