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
import payment.service.PaymentResultResolver;
import payment.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorImpl implements PaymentProcessor {

    private final PaymentService paymentService;
    private final PartnerService partnerService;
    private final PaymentResultResolver paymentResultResolver;

    /*
        Retry here, means from API client
        We provide an option for client to decide whether he wants to retry
        This is done by returning general TIMEOUT_ERROR in response
        We strictly track cases when we can return such an error
     */

    @Override
    public PaymentResultDto process(PaymentDto paymentDto) {

        log.info("Start payment processing, {}", paymentDto); //linking data with opentelemetry trace_id

        Payment retrieved = paymentService.findByKey(paymentDto.getKey());

        if (retrieved != null) {

            log.info("Retrieved payment: {}", retrieved);

            PaymentRuntimeResult paymentRuntimeResult = PaymentRuntimeResult.builder()
                    .paymentDto(paymentDto)
                    .paymentSendResult(retrieved.getPaymentSendResult())
                    .build();

            PaymentSendResult paymentSendResult = partnerService.sendRetry(paymentRuntimeResult);

            paymentRuntimeResult.setPaymentSendResult(paymentSendResult);

            PaymentUpdateResult paymentUpdateResult = paymentService.update(paymentDto, paymentSendResult);

            paymentRuntimeResult.setPaymentUpdateResult(paymentUpdateResult);

            return paymentResultResolver.resolve(paymentRuntimeResult);
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
                .paymentCreateResult(paymentCreateResult)
                .paymentSendResult(paymentSentResult)
                .paymentUpdateResult(paymentUpdateResult)
                .paymentDto(paymentDto)
                .build();

        return paymentResultResolver.resolve(paymentRuntimeResult);
    }
}
