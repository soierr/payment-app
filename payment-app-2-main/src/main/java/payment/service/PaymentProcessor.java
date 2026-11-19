package payment.service;

import payment.dto.PaymentDto;
import payment.dto.PaymentResult;
import payment.dto.PaymentResultDto;

public interface PaymentProcessor {

    PaymentResultDto process(PaymentDto paymentDto);
}
