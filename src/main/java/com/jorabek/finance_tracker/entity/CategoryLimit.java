package com.jorabek.finance_tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_limits")
public class CategoryLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String category;

    @Column(nullable = false)
    private Double limitAmount;

    // Default Constructor
    public CategoryLimit() {
    }

    // Parameterized Constructor
    public CategoryLimit(String category, Double limitAmount) {
        this.category = category;
        this.limitAmount = limitAmount;
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
}
