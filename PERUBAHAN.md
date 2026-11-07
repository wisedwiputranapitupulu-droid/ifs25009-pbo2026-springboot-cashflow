# Perubahan dibanding versi awal

| File | Perubahan | Alasan |
|---|---|---|
| adapter/repository/InMemoryRepository.java | `ArrayList` -> `CopyOnWriteArrayList` | Repository adalah singleton yang dipakai banyak sesi sekaligus. Pada uji 2000 transaksi paralel, versi lama kehilangan data (1832 dari 2000). Versi baru selalu 2000. |
| adapter/repository/TransactionRepository.java | `int idCounter` -> `AtomicInteger` | ID tidak bisa kembar saat banyak pengguna menambah transaksi bersamaan. |
| usecase/FinanceUseCase.java | `getBalance()` satu kali iterasi | Sebelumnya menyalin seluruh list dua kali. Hasilnya sama persis. |
| framework/view/FinanceView.java | Saldo/jumlah dihitung sekali per `reload()`; `role="main"`; `aria-label` pada pencarian, urutan, grid, tombol Hapus; `aria-live` pada status | Mengurangi kerja server per aksi dan menutup kandidat pelanggaran Axe (landmark, label). |
| application.yaml | Kompresi respons, tuning Tomcat, graceful shutdown, JMX dan banner mati | Respons lebih kecil dan startup sedikit lebih ringan. |
| Dockerfile | `-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75 -Xshare:auto` | Start dan respons pertama lebih cepat di container kecil. |

Tidak diubah: pom.xml, domain, presenter, test. API publik tetap sama, jadi semua test lama tetap relevan.

## Perbaikan Axe tahap 2
| File | Perubahan | Alasan |
|---|---|---|
| framework/view/FinanceView.java | `onAttach` memberi `role="region"` dan `aria-label="Status koneksi"` pada `vaadin-connection-indicator` | Axe `region`: elemen itu dibuat Vaadin di luar view sehingga tidak berada di dalam landmark. Java 25 mendukung text block yang dipakai di sini. |
