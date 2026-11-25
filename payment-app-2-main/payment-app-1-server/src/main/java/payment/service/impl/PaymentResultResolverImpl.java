package payment.service.impl;

import org.springframework.stereotype.Component;
import payment.dto.PaymentResultDto;
import payment.model.PaymentCreateResult;
import payment.model.PaymentResultStateDto;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;
import payment.service.PaymentResultResolver;

import java.util.Map;
import java.util.stream.Stream;

@Component
public class PaymentResultResolverImpl implements PaymentResultResolver {

    private final Map<String, PaymentResultStateDto> states;

    public PaymentResultResolverImpl() {

        this.states = Map.of(
                PaymentCreateResult.CREATE_DB_FAILED_TIMEOUT.name(), PaymentResultStateDto.TIMEOUT_ERROR,
                PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT.name(), PaymentResultStateDto.TIMEOUT_ERROR,
                PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT_NO_RETRY_ALLOWED.name(), PaymentResultStateDto.INTERNAL_ERROR,
                PaymentSendResult.SENT_TO_PARTNER_UNKNOWN_NO_RETRY_ALLOWED.name(), PaymentResultStateDto.INTERNAL_ERROR,
                PaymentUpdateResult.UPDATE_DB_FAILED_TIMEOUT.name(), PaymentResultStateDto.INTERNAL_ERROR,
                PaymentUpdateResult.UPDATE_DB_FAILED_OTHER.name(), PaymentResultStateDto.INTERNAL_ERROR);

        /*
            We map by priority from up to bottom see code below
         */
    }

    public PaymentResultDto resolve(PaymentRuntimeResult paymentRuntimeResult) {

        return PaymentResultDto.builder()
                .paymentDate(paymentRuntimeResult.getPaymentDto().getPaymentDate())
                .key(paymentRuntimeResult.getPaymentDto().getKey())
                .amount(paymentRuntimeResult.getPaymentDto().getAmount())
                .paymentResultStateDto(map(paymentRuntimeResult))
                .build();
    }

    private PaymentResultStateDto map(PaymentRuntimeResult paymentRuntimeResult) {

        return Stream.of(paymentRuntimeResult.getPaymentCreateResult(),
                        paymentRuntimeResult.getPaymentSendResult())
                .map(Enum::name)
                .map(states::get)
                .findFirst()
                .orElse(PaymentResultStateDto.PROCESSED);
    }
}
