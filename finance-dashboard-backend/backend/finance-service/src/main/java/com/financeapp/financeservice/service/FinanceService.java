package com.financeapp.financeservice.service;

import com.financeapp.financeservice.dto.FinanceDtos;
import com.financeapp.financeservice.exception.ResourceNotFoundException;
import com.financeapp.financeservice.model.FinancialRecord;
import com.financeapp.financeservice.repository.FinancialRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private final FinancialRecordRepository recordRepository;

    public FinanceService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public List<FinanceDtos.RecordResponse> getAllRecords(
            String type, String category, String from, String to) {

        LocalDate fromDate = (from != null) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null) ? LocalDate.parse(to)   : null;
        FinancialRecord.TransactionType txType =
                (type != null && !type.isBlank()) ? FinancialRecord.TransactionType.valueOf(type.toUpperCase()) : null;

        List<FinancialRecord> records;

        if (txType != null && fromDate != null && toDate != null) {
            records = recordRepository.findByTypeAndDateBetweenOrderByDateDesc(txType, fromDate, toDate);
        } else if (category != null && !category.isBlank() && fromDate != null && toDate != null) {
            records = recordRepository.findByCategoryIgnoreCaseAndDateBetweenOrderByDateDesc(category, fromDate, toDate);
        } else if (fromDate != null && toDate != null) {
            records = recordRepository.findByDateBetweenOrderByDateDesc(fromDate, toDate);
        } else if (txType != null) {
            records = recordRepository.findByType(txType);
        } else if (category != null && !category.isBlank()) {
            records = recordRepository.findByCategoryIgnoreCase(category);
        } else {
            records = recordRepository.findAll();
            records.sort(Comparator.comparing(FinancialRecord::getDate).reversed());
        }

        return records.stream().map(FinanceDtos.RecordResponse::new).collect(Collectors.toList());
    }

    public FinanceDtos.RecordResponse getRecordById(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        return new FinanceDtos.RecordResponse(record);
    }

    public FinanceDtos.RecordResponse createRecord(FinanceDtos.RecordRequest request, String createdBy) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
        record.setCreatedBy(createdBy);
        return new FinanceDtos.RecordResponse(recordRepository.save(record));
    }

    public FinanceDtos.RecordResponse updateRecord(Long id, FinanceDtos.RecordRequest request) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
        return new FinanceDtos.RecordResponse(recordRepository.save(record));
    }

    public void deleteRecord(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }
        recordRepository.deleteById(id);
    }

    // ── Dashboard Summary ─────────────────────────────────────────────────────

    public FinanceDtos.DashboardSummary getDashboardSummary() {
        FinanceDtos.DashboardSummary summary = new FinanceDtos.DashboardSummary();

        // Totals
        BigDecimal totalIncome   = recordRepository.sumAllIncome();
        BigDecimal totalExpenses = recordRepository.sumAllExpenses();

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setNetBalance(totalIncome.subtract(totalExpenses));
        summary.setTotalRecords(recordRepository.count());
        summary.setIncomeCount(recordRepository.countByType(FinancialRecord.TransactionType.INCOME));
        summary.setExpenseCount(recordRepository.countByType(FinancialRecord.TransactionType.EXPENSE));

        // Category totals (all)
        List<Object[]> categoryRows = recordRepository.sumAmountByCategory();
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (Object[] row : categoryRows) {
            categoryTotals.put((String) row[0], (BigDecimal) row[1]);
        }
        summary.setCategoryTotals(categoryTotals);

        // Monthly trends (last 12 months)
        LocalDate fromDate = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        List<Object[]> trendRows = recordRepository.getMonthlyTrends(fromDate);
        List<FinanceDtos.MonthlyTrend> trends = new ArrayList<>();
        for (Object[] row : trendRows) {
            String month       = (String)     row[0];
            BigDecimal income   = (BigDecimal) row[1];
            BigDecimal expenses = (BigDecimal) row[2];
            trends.add(new FinanceDtos.MonthlyTrend(month, income, expenses));
        }
        summary.setMonthlyTrends(trends);

        // Recent transactions
        List<FinanceDtos.RecordResponse> recent = recordRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().map(FinanceDtos.RecordResponse::new).collect(Collectors.toList());
        summary.setRecentTransactions(recent);

        return summary;
    }

    public Map<String, BigDecimal> getCategoryBreakdown(String type) {
        List<Object[]> rows;
        if (type != null && !type.isBlank()) {
            FinancialRecord.TransactionType txType =
                    FinancialRecord.TransactionType.valueOf(type.toUpperCase());
            rows = recordRepository.sumAmountByCategoryAndType(txType);
        } else {
            rows = recordRepository.sumAmountByCategory();
        }

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (BigDecimal) row[1]);
        }
        return result;
    }
}
