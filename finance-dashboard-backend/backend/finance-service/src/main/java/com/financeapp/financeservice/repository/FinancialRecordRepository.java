package com.financeapp.financeservice.repository;

import com.financeapp.financeservice.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    // ── Aggregates ───────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'INCOME'")
    BigDecimal sumAllIncome();

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'EXPENSE'")
    BigDecimal sumAllExpenses();

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.type = :type")
    long countByType(@Param("type") FinancialRecord.TransactionType type);

    // ── Category totals ──────────────────────────────────────────────────────

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r WHERE r.type = :type GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumAmountByCategoryAndType(@Param("type") FinancialRecord.TransactionType type);

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumAmountByCategory();

    // ── Monthly trends ───────────────────────────────────────────────────────

    @Query(value = """
        SELECT
            FORMAT(date, 'yyyy-MM') AS month,
            SUM(CASE WHEN type = 'INCOME'  THEN amount ELSE 0 END) AS income,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS expenses
        FROM financial_records
        WHERE date >= :fromDate
        GROUP BY FORMAT(date, 'yyyy-MM')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrends(@Param("fromDate") LocalDate fromDate);

    // ── Recent activity ──────────────────────────────────────────────────────

    List<FinancialRecord> findTop10ByOrderByCreatedAtDesc();

    // ── Filtered queries ─────────────────────────────────────────────────────

    List<FinancialRecord> findByType(FinancialRecord.TransactionType type);

    List<FinancialRecord> findByCategoryIgnoreCase(String category);

    List<FinancialRecord> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    List<FinancialRecord> findByTypeAndDateBetweenOrderByDateDesc(
            FinancialRecord.TransactionType type, LocalDate from, LocalDate to);

    List<FinancialRecord> findByCategoryIgnoreCaseAndDateBetweenOrderByDateDesc(
            String category, LocalDate from, LocalDate to);
}
