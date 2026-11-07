package org.delcom.ifs25009pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private List<Transaction> sample() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction(1, "A", new BigDecimal("300.00"), TransactionType.EXPENSE));
        list.add(new Transaction(2, "B", new BigDecimal("100.00"), TransactionType.INCOME));
        list.add(new Transaction(3, "C", new BigDecimal("200.00"), TransactionType.EXPENSE));
        list.add(new Transaction(4, "D", new BigDecimal("500.00"), TransactionType.INCOME));
        return list;
    }

    private List<Integer> ids(SortOption option) {
        List<Transaction> list = sample();
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void hasFourOptions() {
        assertEquals(4, SortOption.values().length);
    }

    @Test
    void amountAscending() {
        assertEquals(List.of(2, 3, 1, 4), ids(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescending() {
        assertEquals(List.of(4, 1, 3, 2), ids(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirst() {
        assertEquals(List.of(2, 4, 1, 3), ids(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirst() {
        assertEquals(List.of(1, 3, 2, 4), ids(SortOption.EXPENSE_FIRST));
    }

    @Test
    void labelsAreNotBlank() {
        for (SortOption option : SortOption.values()) {
            assertEquals(false, option.getLabel().isBlank());
        }
    }
}
