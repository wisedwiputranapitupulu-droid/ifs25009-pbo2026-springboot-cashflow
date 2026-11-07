package org.delcom.ifs25009pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveGeneratesIncrementingIds() {
        Transaction first = repository.saveIncome("Gaji", new BigDecimal("100.00"));
        Transaction second = repository.saveExpense("Makan", new BigDecimal("10.00"));
        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(TransactionType.INCOME, first.getType());
        assertEquals(TransactionType.EXPENSE, second.getType());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", new BigDecimal("100.00"));
        List<Transaction> copy = repository.findAll();
        copy.clear();
        assertEquals(1, repository.findAll().size());
        assertNotSame(repository.findAll(), repository.findAll());
    }

    @Test
    void findByIdAndDelete() {
        Transaction saved = repository.saveIncome("Gaji", new BigDecimal("100.00"));
        Optional<Transaction> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertFalse(repository.findById(99).isPresent());

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findByPredicateAndUpdate() {
        Transaction a = repository.saveIncome("Gaji", new BigDecimal("100.00"));
        repository.saveExpense("Makan", new BigDecimal("10.00"));

        List<Transaction> incomes = repository.findBy(Transaction::isIncome);
        assertEquals(1, incomes.size());

        a.changeDescription("Bonus");
        repository.update(a);
        assertEquals("Bonus", repository.findById(a.getId()).get().getDescription());
    }

    @Test
    void idsAreNotReusedAfterDelete() {
        repository.saveIncome("A", new BigDecimal("1.00"));
        repository.deleteById(1);
        Transaction next = repository.saveIncome("B", new BigDecimal("1.00"));
        assertEquals(2, next.getId());
    }
}
