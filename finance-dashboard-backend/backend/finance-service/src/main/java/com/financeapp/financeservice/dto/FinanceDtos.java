package com.financeapp.financeservice.dto;

import com.financeapp.financeservice.model.FinancialRecord;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FinanceDtos {

    // ── Create / Update Request ──────────────────────────────────────────────

    public static class RecordRequest {

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        private BigDecimal amount;

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        private FinancialRecord.TransactionType type;

        @NotBlank(message = "Category is required")
        @Size(max = 100)
        private String category;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @Size(max = 500)
        private String notes;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public FinancialRecord.TransactionType getType() { return type; }
        public void setType(FinancialRecord.TransactionType type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    // ── Record Response ──────────────────────────────────────────────────────

    public static class RecordResponse {
        private Long id;
        private BigDecimal amount;
        private String type;
        private String category;
        private LocalDate date;
        private String notes;
        private String createdBy;
        private String createdAt;

        public RecordResponse(FinancialRecord r) {
            this.id = r.getId();
            this.amount = r.getAmount();
            this.type = r.getType().name();
            this.category = r.getCategory();
            this.date = r.getDate();
            this.notes = r.getNotes();
            this.createdBy = r.getCreatedBy();
            this.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        }

        public Long getId() { return id; }
        public BigDecimal getAmount() { return amount; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public LocalDate getDate() { return date; }
        public String getNotes() { return notes; }
        public String getCreatedBy() { return createdBy; }
        public String getCreatedAt() { return createdAt; }
    }

    // ── Dashboard Summary ────────────────────────────────────────────────────

    public static class DashboardSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
        private long totalRecords;
        private long incomeCount;
        private long expenseCount;
        private Map<String, BigDecimal> categoryTotals;
        private List<MonthlyTrend> monthlyTrends;
        private List<RecordResponse> recentTransactions;

        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
        public BigDecimal getNetBalance() { return netBalance; }
        public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
        public long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
        public long getIncomeCount() { return incomeCount; }
        public void setIncomeCount(long incomeCount) { this.incomeCount = incomeCount; }
        public long getExpenseCount() { return expenseCount; }
        public void setExpenseCount(long expenseCount) { this.expenseCount = expenseCount; }
        public Map<String, BigDecimal> getCategoryTotals() { return categoryTotals; }
        public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) { this.categoryTotals = categoryTotals; }
        public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
        public void setMonthlyTrends(List<MonthlyTrend> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
        public List<RecordResponse> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<RecordResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
    }

    // ── Monthly Trend ────────────────────────────────────────────────────────

    public static class MonthlyTrend {
        private String month;   // e.g. "2024-03"
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;

        public MonthlyTrend(String month, BigDecimal income, BigDecimal expenses) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.net = income.subtract(expenses);
        }

        public String getMonth() { return month; }
        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpenses() { return expenses; }
        public BigDecimal getNet() { return net; }
    }
}
