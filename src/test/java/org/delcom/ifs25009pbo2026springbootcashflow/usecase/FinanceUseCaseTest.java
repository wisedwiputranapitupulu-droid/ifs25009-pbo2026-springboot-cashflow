package org.delcom.ifs25009pbo2026springbootcashflow.usecase;

import org.delcom.ifs25009pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    private void seed() throws Exception {
        useCase.addIncome("Gaji Bulanan", "5000000");
        useCase.addExpense("Makan Siang", "25000");
        useCase.addIncome("Bonus", "100.50");
        useCase.addExpense("Bensin", "1000000");
    }

    @Test
    void addIncomeAndExpenseUpdateBalance() throws Exception {
        assertEquals(new BigDecimal("0"), useCase.getBalance());
        Transaction income = useCase.addIncome("  Gaji  ", "1000000");
        assertEquals("Gaji", income.getDescription());
        assertEquals(new BigDecimal("1000000.00"), income.getAmount());
        useCase.addExpense("Makan", "25000.505");

        assertEquals(new BigDecimal("1000000.00"), useCase.getTotalIncome());
        assertEquals(new BigDecimal("25000.51"), useCase.getTotalExpense());
        assertEquals(new BigDecimal("974999.49"), useCase.getBalance());
        assertEquals(2, useCase.countTransactions());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void removeTransactionKeepsBalanceConsistent() throws Exception {
        useCase.addIncome("Gaji", "1000");
        Transaction expense = useCase.addExpense("Makan", "400");
        assertEquals(new BigDecimal("600.00"), useCase.getBalance());

        useCase.removeTransaction(expense.getId());
        assertEquals(new BigDecimal("1000.00"), useCase.getBalance());
    }

    @Test
    void rejectsEmptyDescription() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("   ", "100"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("", "100"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x".repeat(101), "100"));
        assertEquals(0, useCase.countTransactions());
    }

    @Test
    void rejectsInvalidAmounts() {
        for (String bad : new String[]{null, "", "  ", "abc", "12abc", "1e3", "1,000", "Rp100", "NaN", "0", "0.00",
                "0.001", "-5", "-0.5", "1000000000000.01"}) {
            assertThrows(ValidationException.class, () -> useCase.addIncome("Uji", bad), "jumlah: " + bad);
            assertThrows(ValidationException.class, () -> useCase.addExpense("Uji", bad), "jumlah: " + bad);
        }
        assertEquals(0, useCase.countTransactions());
    }

    @Test
    void amountMessagesAreSpecific() {
        ValidationException nonNumeric = assertThrows(ValidationException.class, () -> useCase.addIncome("A", "abc"));
        assertTrue(nonNumeric.getMessage().contains("angka"));
        ValidationException nonPositive = assertThrows(ValidationException.class, () -> useCase.addIncome("A", "-1"));
        assertTrue(nonPositive.getMessage().contains("lebih besar dari nol"));
    }

    @Test
    void acceptsMaximumAmount() throws Exception {
        Transaction t = useCase.addIncome("Maks", "1000000000000");
        assertEquals(new BigDecimal("1000000000000.00"), t.getAmount());
    }

    @Test
    void removeInvalidOrMissingId() throws Exception {
        useCase.addIncome("Gaji", "100");
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(-3));
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        assertEquals(99, ex.getId());
        assertEquals(1, useCase.countTransactions());
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotChangeData() throws Exception {
        seed();
        assertEquals(1, useCase.searchTransactions("GAJI").size());
        assertEquals(1, useCase.searchTransactions("  makan ").size());
        assertEquals(4, useCase.searchTransactions("").size());
        assertEquals(4, useCase.searchTransactions(null).size());
        assertEquals(0, useCase.searchTransactions("tidak ada").size());
        assertEquals(4, useCase.countTransactions());
    }

    @Test
    void sortUsesDomainSortOptions() throws Exception {
        seed();
        assertEquals(List.of(3, 2, 4, 1), ids(useCase.sortTransactions(SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 4, 2, 3), ids(useCase.sortTransactions(SortOption.AMOUNT_DESC)));
        assertEquals(List.of(1, 3, 2, 4), ids(useCase.sortTransactions(SortOption.INCOME_FIRST)));
        assertEquals(List.of(2, 4, 1, 3), ids(useCase.sortTransactions(SortOption.EXPENSE_FIRST)));
    }

    @Test
    void getTransactionsCombinesSearchAndSort() throws Exception {
        seed();
        assertEquals(List.of(1, 2, 3, 4), ids(useCase.getTransactions(null, null)));
        assertEquals(List.of(1, 2), ids(useCase.getTransactions("an", null)));
        assertEquals(List.of(2, 1), ids(useCase.getTransactions("an", SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 2), ids(useCase.getTransactions("an", SortOption.AMOUNT_DESC)));
    }

    private List<Integer> ids(List<Transaction> list) {
        return list.stream().map(Transaction::getId).toList();
    }
}
