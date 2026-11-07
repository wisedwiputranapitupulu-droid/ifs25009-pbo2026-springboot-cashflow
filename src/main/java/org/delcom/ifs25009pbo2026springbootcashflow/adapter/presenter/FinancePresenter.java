package org.delcom.ifs25009pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat uang dengan pemisah ribuan (koma) dan dua digit desimal, contoh: Rp 1,000,000.00. */
    public String formatMoney(BigDecimal amount) {
        // DecimalFormat tidak thread-safe, jadi dibuat per pemanggilan.
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String body = format.format(amount.abs());
        return (amount.signum() < 0 ? "-Rp " : "Rp ") + body;
    }

    /** Memformat satu transaksi menjadi baris teks: [id] keterangan | Rp jumlah | [jenis]. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction)
        );
    }

    /** Memformat jenis transaksi (Pemasukan / Pengeluaran). */
    public String formatType(Transaction transaction) {
        return transaction.getType().getLabel();
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo, contoh: Saldo: Rp 4,975,000.00. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /**
     * Memformat daftar transaksi menjadi teks multi-baris.
     * Mengembalikan pesan kosong jika list tidak berisi data.
     */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", showEmptyTransactions());
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", showTransactionNotFound());
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", showEmptyTransactions());
    }

    /** Judul daftar: judul biasa, atau judul hasil pencarian bila ada kata kunci. */
    public String showListTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "Daftar Transaksi";
        }
        return "Hasil Pencarian: \"" + keyword.trim() + "\"";
    }

    /** Pesan saat belum ada data transaksi. */
    public String showEmptyTransactions() {
        return "- Data transaksi belum tersedia!";
    }

    /** Pesan saat pencarian tidak menemukan transaksi. */
    public String showTransactionNotFound() {
        return "- Transaksi tidak ditemukan!";
    }

    /** Pesan sukses setelah menambah pemasukan. */
    public String showAddIncomeSuccess(Transaction transaction) {
        return "Berhasil menambah pemasukan: " + format(transaction);
    }

    /** Pesan sukses setelah menambah pengeluaran. */
    public String showAddExpenseSuccess(Transaction transaction) {
        return "Berhasil menambah pengeluaran: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan saat pilihan tidak dikenali. */
    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
