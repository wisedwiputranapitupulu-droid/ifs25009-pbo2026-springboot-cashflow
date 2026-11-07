package org.delcom.ifs25009pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementasi repository transaksi menggunakan basis generic {@link InMemoryRepository}.
 * Data disimpan di memori (hilang saat aplikasi di-restart).
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    /** Penghitung ID otomatis, bertambah setiap kali transaksi baru disimpan. */
    private final AtomicInteger idCounter = new AtomicInteger();

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction saveIncome(String description, BigDecimal amount) {
        return save(new Transaction(nextId(), description, amount, TransactionType.INCOME));
    }

    @Override
    public Transaction saveExpense(String description, BigDecimal amount) {
        return save(new Transaction(nextId(), description, amount, TransactionType.EXPENSE));
    }

    /** Menghasilkan ID unik berikutnya. */
    private int nextId() {
        return idCounter.incrementAndGet();
    }
}
