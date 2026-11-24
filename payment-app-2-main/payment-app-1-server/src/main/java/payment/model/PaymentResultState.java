package payment.model;

public enum PaymentResultState {
    PROCESSED,
    RECOVERABLE_ERROR,
    VALIDATION_ERROR,
    INTERNAL_ERROR
}
