package payment.service;

import payment.dto.PaymentDto;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;

public interface PartnerService {

    PaymentSendResult send(PaymentDto paymentDto);

    PaymentSendResult sendRetry(PaymentRuntimeResult paymentRuntimeResult);
}
