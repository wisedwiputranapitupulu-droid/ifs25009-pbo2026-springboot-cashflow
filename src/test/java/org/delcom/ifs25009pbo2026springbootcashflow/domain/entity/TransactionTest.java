package org.delcom.ifs25009pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void incomeHasPositiveSignedAmount() {
        Transaction t = new Transaction(1, "Gaji", new BigDecimal("1000.00"), TransactionType.INCOME);
        assertTrue(t.isIncome());
        assertFalse(t.isExpense());
        assertEquals(new BigDecimal("1000.00"), t.signedAmount());
        assertNotNull(t.getCreatedAt());
    }

    @Test
    void expenseHasNegativeSignedAmount() {
        Transaction t = new Transaction(2, "Makan", new BigDecimal("25.50"), TransactionType.EXPENSE);
        assertTrue(t.isExpense());
        assertFalse(t.isIncome());
        assertEquals(new BigDecimal("-25.50"), t.signedAmount());
    }

    @Test
    void changeMethodsUpdateFieldsButNotId() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 4, 10, 30);
        Transaction t = new Transaction(3, "Lama", new BigDecimal("10.00"), TransactionType.INCOME, time);

        t.changeDescription("Baru");
        t.changeAmount(new BigDecimal("99.00"));
        t.changeType(TransactionType.EXPENSE);

        assertEquals(3, t.getId());
        assertEquals("Baru", t.getDescription());
        assertEquals(new BigDecimal("99.00"), t.getAmount());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals(time, t.getCreatedAt());
    }

    @Test
    void typeLabelsAreIndonesian() {
        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.getLabel());
    }
}
