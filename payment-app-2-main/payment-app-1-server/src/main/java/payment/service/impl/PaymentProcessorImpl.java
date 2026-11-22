package payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import payment.dto.PaymentDto;
import payment.dto.PaymentResultDto;
import payment.model.Payment;
import payment.model.PaymentCreateResult;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;
import payment.service.PartnerService;
import payment.service.PaymentProcessor;
import payment.service.PaymentRecoveryService;
import payment.service.PaymentResultResolver;
import payment.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorImpl implements PaymentProcessor {

    private final PaymentService paymentService;
    private final PartnerService partnerService;
    private final PaymentRecoveryService paymentRecoveryService;
    private final PaymentResultResolver paymentResultResolver;

    @Override
    public PaymentResultDto process(PaymentDto paymentDto) {

        log.info("Start payment processing, {}", paymentDto); //linking data with opentelemetry trace_id

        Payment retrieved = paymentService.findByKey(paymentDto.getKey());

        if (retrieved != null) {

            PaymentRuntimeResult paymentRuntimeResult = PaymentRuntimeResult.builder()
                    .paymentDto(paymentDto)
                    .paymentSendResult(retrieved.getPaymentSendResult())
                    .build();

            PaymentRuntimeResult processed = paymentRecoveryService.process(paymentRuntimeResult);

            return paymentResultResolver.resolve(processed);
        }

        PaymentCreateResult paymentCreateResult = paymentService.create(paymentDto);

        if (paymentCreateResult != PaymentCreateResult.CREATE_DB_SUCCESS) {

            PaymentRuntimeResult paymentRuntimeResult = PaymentRuntimeResult.builder()
                    .paymentDto(paymentDto)
                    .paymentCreateResult(paymentCreateResult)
                    .build();

            return paymentResultResolver.resolve(paymentRuntimeResult);
        }

        PaymentSendResult paymentSentResult = partnerService.send(paymentDto);

        PaymentUpdateResult paymentUpdateResult = paymentService.update(paymentDto, paymentSentResult);

        PaymentRuntimeResult paymentRuntimeResult = PaymentRuntimeResult.builder()
                .paymentSendResult(paymentSentResult)
                .paymentUpdateResult(paymentUpdateResult)
                .build();

        return paymentResultResolver.resolve(paymentRuntimeResult);
    }

}
