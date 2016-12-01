package ru.unn.agile.PersonalFinance.ViewModel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import ru.unn.agile.PersonalFinance.Model.Category;
import ru.unn.agile.PersonalFinance.Model.ExternalTransaction;

import java.util.Objects;

public class ExternalTransactionViewModel extends TransactionViewModel {
    private final LedgerViewModel parentLedger;
    private ExternalTransaction modelExternalTransaction;

    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty counterpartyProperty = new SimpleStringProperty();
    private final ObjectProperty<CategoryViewModel> categoryProperty =
            new SimpleObjectProperty<>();

    public ExternalTransactionViewModel(final LedgerViewModel parentLedger) {
        Objects.requireNonNull(parentLedger);
        this.parentLedger = parentLedger;
        setUpBindings();

        setCounterparty("<Most frequent counterparty>");
        setCategory(new CategoryViewModel());
    }

    // region Properties for Binding

    public final String getDescription() {
        return this.descriptionProperty.get();
    }

    public final StringProperty descriptionProperty() {
        return this.descriptionProperty;
    }

    public final void setDescription(final String description) {
        this.descriptionProperty.set(description);
    }

    public final StringProperty counterpartyProperty() {
        return this.counterpartyProperty;
    }

    public final String getCounterparty() {
        return this.counterpartyProperty.get();
    }

    public final void setCounterparty(final String counterparty) {
        this.counterpartyProperty.set(counterparty);
    }

    public final ObjectProperty<CategoryViewModel> categoryProperty() {
        return this.categoryProperty;
    }

    public final CategoryViewModel getCategory() {
        return this.categoryProperty.get();
    }

    public final void setCategory(final CategoryViewModel category) {
        this.categoryProperty.set(category);
    }

    // endregion

    ExternalTransaction getModelExternalTransaction() {
        if (modelExternalTransaction == null) {
            throw new UnsupportedOperationException("Transaction should be "
                    + "saved before getting model transaction");
        }
        return modelExternalTransaction;
    }

    @Override
    protected void saveInternal() {
        AccountViewModel parentAccount = parentLedger.getSelectedAccount();
        if (parentAccount == null) {
            throw new UnsupportedOperationException("Account should be selected before saving");
        }

        modelExternalTransaction = buildExternalTransaction();
        parentAccount.addExternalTransaction(this);
    }

    private ExternalTransaction buildExternalTransaction() {
        ExternalTransaction.Builder transactionBuilder = getIsIncome()
                ? ExternalTransaction.incomeBuilder(getAmount())
                : ExternalTransaction.expenseBuilder(getAmount());

        return transactionBuilder
                .date(GregorianCalendarHelper.convertFromLocalDate(getDate()))
                .category(getModelCategory())
                .description(getDescription())
                .counterparty(getCounterparty())
                .build();
    }

    private Category getModelCategory() {
        CategoryViewModel categoryViewModel = getCategory();
        return categoryViewModel.getModelCategory();
    }

    private void setUpBindings() {
        BooleanBinding isCounterpartyEmptyBinding = Bindings.createBooleanBinding(() ->
                isCounterpartyEmpty(), counterpartyProperty);

        BooleanBinding isAbleToSaveBinding =
                amountProperty().greaterThan(0)
                .and(isCounterpartyEmptyBinding.not())
                .and(categoryProperty().isNotNull());

        isAbleToSaveProperty.bind(isAbleToSaveBinding);
    }

    private boolean isCounterpartyEmpty() {
        return  getCounterparty() == null ||
                getCounterparty().trim().isEmpty();
    }
}
