package payment.service;

import payment.dto.PaymentResultDto;
import payment.model.PaymentRuntimeResult;

public interface PaymentResultResolver {

    PaymentResultDto resolve(PaymentRuntimeResult paymentRuntimeResult);
}
