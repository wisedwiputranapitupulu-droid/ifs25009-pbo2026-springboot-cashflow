package org.delcom.ifs25009pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Setiap jenis membawa label tampilan berbahasa Indonesia sehingga teks
 * tidak tersebar sebagai string literal di presenter maupun view.
 */
public enum TransactionType {
    /** Uang masuk (menambah saldo). */
    INCOME("Pemasukan"),

    /** Uang keluar (mengurangi saldo). */
    EXPENSE("Pengeluaran");

    /** Label yang ditampilkan kepada pengguna. */
    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Mengembalikan label tampilan jenis transaksi. */
    public String getLabel() {
        return label;
    }
}
