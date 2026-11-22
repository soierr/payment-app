package payment.model;

public enum PaymentResultState {
    PROCESSED,
    RECOVERABLE_ERROR,
    VALIDATION_ERROR,
    INTERNAL_ERROR,
    SAVE_IN_DB_ERROR,
    UPDATE_DB_ERROR
}
