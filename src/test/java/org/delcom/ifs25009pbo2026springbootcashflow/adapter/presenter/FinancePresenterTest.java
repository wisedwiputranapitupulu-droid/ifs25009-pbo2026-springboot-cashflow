package org.delcom.ifs25009pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {
    private final FinancePresenter presenter = new FinancePresenter();

    private Transaction income() {
        return new Transaction(1, "Gaji", new BigDecimal("1000000.00"), TransactionType.INCOME,
                LocalDateTime.of(2026, 11, 4, 9, 5));
    }

    private Transaction expense() {
        return new Transaction(2, "Makan", new BigDecimal("25000.50"), TransactionType.EXPENSE,
                LocalDateTime.of(2026, 11, 4, 12, 0));
    }

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 0.00", presenter.formatMoney(BigDecimal.ZERO));
        assertEquals("Rp 4,975,000.00", presenter.formatMoney(new BigDecimal("4975000")));
        assertEquals("-Rp 500.00", presenter.formatMoney(new BigDecimal("-500")));
    }

    @Test
    void formatTransactionLine() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income()));
        assertEquals("[2] Makan | Rp 25,000.50 | [Pengeluaran]", presenter.format(expense()));
    }

    @Test
    void formatBalanceAndDates() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new BigDecimal("4975000")));
        assertEquals("04/11/2026 09:05", presenter.formatCreatedAt(income()));
        assertEquals("Pengeluaran", presenter.formatType(expense()));
    }

    @Test
    void listFormatting() {
        String all = presenter.showTransactions(List.of(income(), expense()));
        assertTrue(all.startsWith("Daftar Transaksi:"));
        assertTrue(all.contains("[1] Gaji"));
        assertTrue(all.contains("[2] Makan"));

        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", presenter.showTransactions(List.of()));
        assertEquals("Hasil Pencarian: \"xyz\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "xyz"));
        assertTrue(presenter.showSearchResults(List.of(income()), "gaji").contains("[1] Gaji"));
        assertTrue(presenter.showSortedTransactions(List.of(income())).startsWith("Daftar Transaksi (Terurut):"));
        assertEquals("Daftar Transaksi (Terurut):\n- Data transaksi belum tersedia!",
                presenter.showSortedTransactions(List.of()));
    }

    @Test
    void titlesAndMessages() {
        assertEquals("Daftar Transaksi", presenter.showListTitle(null));
        assertEquals("Daftar Transaksi", presenter.showListTitle("  "));
        assertEquals("Hasil Pencarian: \"gaji\"", presenter.showListTitle(" gaji "));
        assertEquals("- Data transaksi belum tersedia!", presenter.showEmptyTransactions());
        assertEquals("- Transaksi tidak ditemukan!", presenter.showTransactionNotFound());
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddIncomeSuccess(income()));
        assertEquals("Berhasil menambah pengeluaran: [2] Makan | Rp 25,000.50 | [Pengeluaran]",
                presenter.showAddExpenseSuccess(expense()));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("[!] Pilihan tidak dimengerti.", presenter.showInvalidChoice());
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] salah", presenter.showValidationError("salah"));
    }
}
