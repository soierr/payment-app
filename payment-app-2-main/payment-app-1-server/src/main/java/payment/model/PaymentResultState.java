package payment.model;

public enum PaymentResultState {
    PROCESSED,
    TIMEOUT_ERROR,
    VALIDATION_ERROR,
    INTERNAL_ERROR
}
