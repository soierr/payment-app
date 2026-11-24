package payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import payment.dto.PaymentDto;
import payment.model.Payment;
import payment.model.PaymentCreateResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;
import payment.repository.PaymentRepository;
import payment.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentCreateResult create(PaymentDto paymentDto) {

        Payment payment = new Payment();
        payment.setKey(paymentDto.getKey());
        payment.setAmount(paymentDto.getAmount());
        payment.setPartner(paymentDto.getPartner());
        payment.setPaymentDate(paymentDto.getPaymentDate());

        try {

            paymentRepository.save(payment);
            return PaymentCreateResult.CREATE_DB_SUCCESS;

        } catch (RuntimeException e) {

            log.error("Payment creation failed", e);

            //map error here: recoverable/non-recoverable
            return PaymentCreateResult.CREATE_DB_FAILED_OTHER;
        }
    }

    @Override
    public PaymentUpdateResult update(PaymentDto paymentDto, PaymentSendResult paymentSendResult) {

        Payment payment = new Payment();
        payment.setKey(paymentDto.getKey());
        payment.setAmount(paymentDto.getAmount());
        payment.setPartner(paymentDto.getPartner());
        payment.setPaymentDate(paymentDto.getPaymentDate());
        payment.setPaymentSendResult(paymentSendResult);

        try {

            paymentRepository.save(payment);
            return PaymentUpdateResult.UPDATE_DB_SUCCESS;

        } catch (RuntimeException e) {

            log.error("Payment update failed", e);

            //map error here: recoverable/non-recoverable
            return PaymentUpdateResult.UPDATE_DB_FAILED_OTHER;
        }
    }

    @Override
    public Payment findByKey(String key) {
        return paymentRepository.findByKey(key);
    }
}
