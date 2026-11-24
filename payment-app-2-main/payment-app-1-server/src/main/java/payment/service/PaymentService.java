package payment.service;

import payment.dto.PaymentDto;
import payment.model.Payment;
import payment.model.PaymentCreateResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;

public interface PaymentService {

    PaymentCreateResult create(PaymentDto paymentDto);

    PaymentUpdateResult update(PaymentDto paymentDto, PaymentSendResult paymentSendResult);

    Payment findByKey(String key);

}
