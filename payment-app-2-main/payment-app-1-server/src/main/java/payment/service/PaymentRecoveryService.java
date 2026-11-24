package payment.service;

import payment.model.PaymentRuntimeResult;

public interface PaymentRecoveryService {

    PaymentRuntimeResult process(PaymentRuntimeResult paymentRuntimeResult);
}
