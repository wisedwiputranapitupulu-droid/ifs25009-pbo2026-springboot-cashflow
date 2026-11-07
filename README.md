# ifs25009-pbo2026-springboot-cashflow

Aplikasi web pencatat transaksi keuangan pribadi (Cashflow) — Spring Boot 4.1.2, Maven, Vaadin 25, Java 25,
dengan Clean Architecture (domain, usecase, adapter, framework). Data disimpan di memori (reset saat restart).

- Group: `org.delcom`
- Package: `org.delcom.ifs25009pbo2026springbootcashflow`

## Fitur
- Tambah Pemasukan / Tambah Pengeluaran (dialog form)
- Cari berdasarkan keterangan (case-insensitive), urutkan lewat dropdown `SortOption` atau klik header kolom
- Kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, saldo
- Hapus transaksi dengan dialog konfirmasi
- Validasi: keterangan kosong, jumlah non-positif / non-numerik, ID tidak valid / tidak ditemukan
- Format uang: `Rp 1,000,000.00`

## Struktur
```
src/main/java/org/delcom/ifs25009pbo2026springbootcashflow/
├── domain/
│   ├── entity/       Transaction, TransactionType, SortOption
│   ├── exception/    DomainException, EntityNotFoundException, ValidationException
│   └── repository/   IRepository<T, ID>, ITransactionRepository
├── usecase/          FinanceUseCase (termasuk getBalance)
├── adapter/
│   ├── repository/   InMemoryRepository<T, ID>, TransactionRepository
│   └── presenter/    FinancePresenter
├── framework/view/   FinanceView (Vaadin, @Route(""))
├── config/           AppConfig (composition root)
├── DotEnv.java       memuat .env ke System properties
└── App.java          entry point
```

## Menjalankan
Port diatur lewat `.env` (`APP_PORT=8080`, lihat `.env.example`).

```
.\mvnw.cmd spring-boot:run                 # development (Windows)
./mvnw spring-boot:run                     # Linux / macOS
```

Build JAR (wajib profil production Vaadin):

```
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25009-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Unit test + laporan Jacoco: `.\mvnw.cmd clean verify` → `target/site/jacoco/index.html`
