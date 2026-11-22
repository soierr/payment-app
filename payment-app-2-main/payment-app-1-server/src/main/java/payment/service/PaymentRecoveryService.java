package payment.service;

import payment.dto.PaymentDto;
import payment.dto.PaymentResult;
import payment.dto.PaymentResultDto;
import payment.model.PaymentRuntimeResult;

public interface PaymentRecoveryService {

    PaymentRuntimeResult process(PaymentRuntimeResult paymentRuntimeResult);
}
