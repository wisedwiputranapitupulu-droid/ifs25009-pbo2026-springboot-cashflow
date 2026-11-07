package org.delcom.ifs25009pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa label dan comparator-nya sendiri sehingga logika
 * pengurutan terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {
    /** Jumlah terkecil ke terbesar. */
    AMOUNT_ASC("Jumlah Terkecil",
            Comparator.comparing(Transaction::getAmount).thenComparing(Transaction::getId)),

    /** Jumlah terbesar ke terkecil. */
    AMOUNT_DESC("Jumlah Terbesar",
            Comparator.comparing(Transaction::getAmount).reversed().thenComparing(Transaction::getId)),

    /** Tampilkan pemasukan terlebih dahulu. */
    INCOME_FIRST("Pemasukan Dulu",
            Comparator.comparing(Transaction::getType).thenComparing(Transaction::getId)),

    /** Tampilkan pengeluaran terlebih dahulu. */
    EXPENSE_FIRST("Pengeluaran Dulu",
            Comparator.comparing(Transaction::getType).reversed().thenComparing(Transaction::getId));

    /** Label yang ditampilkan kepada pengguna. */
    private final String label;

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** Mengembalikan label tampilan opsi ini. */
    public String getLabel() {
        return label;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
