package payment.service;

import payment.dto.PaymentDto;
import payment.dto.PaymentResult;
import payment.dto.PaymentResultDto;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;

public interface PaymentResultResolver {

    PaymentResultDto resolve(PaymentRuntimeResult paymentRuntimeResult);
}
