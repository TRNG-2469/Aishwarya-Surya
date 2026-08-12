package com.aishwarya.ers.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reimbursement {

    private int id;
    private int userId;
    private BigDecimal amount;
    private String description;
    private ReimbursementType type;
    private ReimbursementStatus status;
    private Integer resolverId;
    private String createdAt;

    public Reimbursement() {
    }

    public Reimbursement(int id, int userId, BigDecimal amount, String description,
                         ReimbursementType type, ReimbursementStatus status,
                         Integer resolverId, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.status = status;
        this.resolverId = resolverId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReimbursementType getType() {
        return type;
    }

    public void setType(ReimbursementType type) {
        this.type = type;
    }

    public ReimbursementStatus getStatus() {
        return status;
    }

    public void setStatus(ReimbursementStatus status) {
        this.status = status;
    }

    public Integer getResolverId() {
        return resolverId;
    }

    public void setResolverId(Integer resolverId) {
        this.resolverId = resolverId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
