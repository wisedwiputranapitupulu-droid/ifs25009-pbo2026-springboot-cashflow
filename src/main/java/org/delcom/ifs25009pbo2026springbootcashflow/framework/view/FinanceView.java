package org.delcom.ifs25009pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25009pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25009pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow (catatan keuangan).
 * Fitur: kartu ringkasan (total transaksi, pemasukan, pengeluaran, saldo), pencarian keterangan,
 * pengurutan lewat dropdown {@link SortOption} atau klik header kolom grid, tambah pemasukan /
 * pengeluaran lewat dialog, dan hapus dengan dialog konfirmasi.
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan")
public class FinanceView extends VerticalLayout {
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();

    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span("Rp 0.00");
    private final Span totalExpense = new Span("Rp 0.00");
    private final Span balanceValue = new Span("Rp 0.00");

    private static final String EMPTY_SORT_CAPTION = "Urutan awal";

    /** Kata kunci pencarian yang sedang aktif (kosong = tanpa filter). */
    private String activeKeyword = "";

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Landmark utama untuk pembaca layar / audit aksesibilitas (axe: landmark-one-main, region).
        getElement().setAttribute("role", "main");

        H1 title = new H1("Cashflow - Catatan Keuangan");
        title.getStyle().set("margin", "0");

        configureGrid();
        configureToolbar();

        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    /**
     * Indikator koneksi Vaadin dibuat di luar view (langsung di body) sehingga tidak berada
     * di dalam landmark apa pun (axe: region). Beri role region + label agar menjadi landmark.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getUI().getPage().executeJs("""
                const apply = () => {
                  const el = document.querySelector('vaadin-connection-indicator');
                  if (!el) { return false; }
                  el.setAttribute('role', 'region');
                  el.setAttribute('aria-label', 'Status koneksi');
                  return true;
                };
                if (!apply()) {
                  const observer = new MutationObserver(() => { if (apply()) { observer.disconnect(); } });
                  observer.observe(document.body, { childList: true, subtree: true });
                }
                """);
    }

    // ------------------------------------------------------------------ ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard("Saldo", balanceValue, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        summary.getStyle().set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "180px")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary(int count, BigDecimal balance) {
        totalCount.setText(String.valueOf(count));
        totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balanceValue.setText(presenter.formatMoney(balance));
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(transaction -> presenter.formatCreatedAt(transaction))
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.getElement().setAttribute("aria-label", "Daftar transaksi");
        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
        grid.setMinHeight("300px");
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? "#166534" : "#991B1B");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getElement().setAttribute("aria-label",
                "Hapus transaksi " + transaction.getId() + ": " + transaction.getDescription());
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.getElement().setAttribute("aria-label", "Cari keterangan transaksi");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> searchTransactions());

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.getElement().setAttribute("aria-label", "Urutkan transaksi");
        sortSelect.setItems(SortOption.values());
        // Select memanggil generator ini dengan null untuk item "kosong" (tanpa pengurutan),
        // sehingga method reference SortOption::getLabel langsung memicu NullPointerException.
        sortSelect.setItemLabelGenerator(option -> option == null ? EMPTY_SORT_CAPTION : option.getLabel());
        sortSelect.setEmptySelectionCaption(EMPTY_SORT_CAPTION);
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.addValueChangeListener(event -> reload());
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        statusLabel.getElement().setAttribute("aria-live", "polite");
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        incomeButton.getStyle().set("background-color", "#166534").set("color", "#FFFFFF");

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        expenseButton.getStyle().set("background-color", "#991B1B").set("color", "#FFFFFF");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return toolbar;
    }

    // ------------------------------------------------------------------ dialog

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(100);
        descriptionField.setClearButtonVisible(true);
        descriptionField.setPlaceholder(income ? "Contoh: Gaji bulanan" : "Contoh: Makan siang");

        TextField amountField = new TextField("Jumlah");
        amountField.setWidthFull();
        amountField.setPlaceholder("Contoh: 150000");
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setAllowedCharPattern("[0-9.]");

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        form.setSpacing(true);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");
        dialog.add(form);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(income
                        ? presenter.showAddIncomeSuccess(created)
                        : presenter.showAddExpenseSuccess(created));
                dialog.close();
                reload();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
        descriptionField.focus();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription() + "\" ("
                        + presenter.formatMoney(transaction.getAmount()) + ", ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Menghapus Transaksi");
        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");
        dialog.add(message);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    // ------------------------------------------------------------------ aksi

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
        reload();
    }

    private void searchTransactions() {
        String keyword = searchField.getValue() == null ? "" : searchField.getValue().trim();
        if (keyword.equals(activeKeyword)) {
            return;
        }
        activeKeyword = keyword;
        reload();
    }

    /** Mengembalikan tampilan ke kondisi awal: tanpa pencarian dan tanpa pengurutan. */
    private void refreshAll() {
        activeKeyword = "";
        searchField.clear();
        sortSelect.clear();
        reload();
    }

    /** Memuat ulang grid sesuai kata kunci dan opsi urutan yang sedang aktif. */
    private void reload() {
        List<Transaction> shown = financeUseCase.getTransactions(activeKeyword, sortSelect.getValue());
        int count = financeUseCase.countTransactions();
        BigDecimal balance = financeUseCase.getBalance();
        boolean noData = count == 0;
        emptyState.setText(noData ? presenter.showEmptyTransactions() : presenter.showTransactionNotFound());

        grid.setItems(shown);
        updateSummary(count, balance);

        String title = presenter.showListTitle(activeKeyword);
        statusLabel.setText(title + " — " + presenter.formatBalance(balance));
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
