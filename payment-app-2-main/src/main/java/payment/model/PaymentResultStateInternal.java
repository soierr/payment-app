package payment.model;

/*
    Not for exposing outside to API
 */
public enum PaymentResultStateInternal {
    SENT_TO_PARTNER_SUCCESS,
    SENT_TO_PARTNER_FAILED_TIMEOUT,
    SENT_TO_PARTNER_FAILED_OTHER,
    UPDATE_DB_FAILED,
    PROCESSING_COMPLETED
}
