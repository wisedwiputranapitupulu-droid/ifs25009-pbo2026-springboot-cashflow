package org.delcom.ifs25009pbo2026springbootcashflow.usecase;

import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Use case yang menangani logika bisnis aplikasi cashflow.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Panjang maksimum keterangan transaksi. */
    static final int MAX_DESCRIPTION_LENGTH = 100;

    /** Batas atas jumlah transaksi (1 triliun). */
    static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000");

    /** Format angka yang diterima: bilangan bulat / desimal, boleh diawali tanda minus. */
    private static final Pattern NUMERIC = Pattern.compile("-?\\d+(\\.\\d+)?");

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan setelah validasi. */
    public Transaction addIncome(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return transactionRepository.saveIncome(cleanDescription, amount);
    }

    /** Menambahkan transaksi pengeluaran setelah validasi. */
    public Transaction addExpense(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return transactionRepository.saveExpense(cleanDescription, amount);
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     *
     * @throws ValidationException     jika ID tidak valid (kurang dari 1)
     * @throws EntityNotFoundException jika transaksi dengan ID tersebut tidak ada
     */
    public void removeTransaction(int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid (harus lebih dari 0)");
        }
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return transactionRepository.findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan semua transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return getTransactions(null, option);
    }

    /**
     * Mengambil transaksi yang sudah difilter kata kunci dan/atau diurutkan.
     * Kata kunci kosong berarti tanpa filter; opsi {@code null} berarti urutan asli (berdasarkan ID).
     */
    public List<Transaction> getTransactions(String keyword, SortOption option) {
        List<Transaction> result = searchTransactions(keyword);
        if (option == null) {
            return result;
        }
        return result.stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Saldo saat ini: total pemasukan dikurangi total pengeluaran. */
    public BigDecimal getBalance() {
        // Satu kali iterasi (sebelumnya findAll() disalin dua kali lewat getTotalIncome + getTotalExpense).
        return transactionRepository.findAll().stream()
                .map(Transaction::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Jumlah seluruh transaksi yang tercatat. */
    public int countTransactions() {
        return transactionRepository.findAll().size();
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        String clean = description.trim();
        if (clean.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException(
                    "Keterangan transaksi maksimal " + MAX_DESCRIPTION_LENGTH + " karakter");
        }
        return clean;
    }

    private BigDecimal parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Jumlah transaksi tidak boleh kosong");
        }
        String clean = amountText.trim();
        if (!NUMERIC.matcher(clean).matches()) {
            throw new ValidationException("Jumlah harus berupa angka (contoh: 150000 atau 150000.50)");
        }
        BigDecimal amount = new BigDecimal(clean).setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari nol");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Jumlah melebihi batas maksimum (Rp 1,000,000,000,000.00)");
        }
        return amount;
    }
}
