package payment.service.impl;

import org.springframework.stereotype.Component;
import payment.dto.PaymentResultDto;
import payment.model.PaymentCreateResult;
import payment.model.PaymentResultState;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;
import payment.service.PaymentResultResolver;

import java.util.Map;
import java.util.stream.Stream;

@Component
public class PaymentResultResolverImpl implements PaymentResultResolver {

    private final Map<String, PaymentResultState> states;

    public PaymentResultResolverImpl() {

        this.states = Map.of(
                PaymentCreateResult.CREATE_DB_FAILED_TIMEOUT.name(), PaymentResultState.TIMEOUT_ERROR,
                PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT.name(), PaymentResultState.TIMEOUT_ERROR,
                PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT_NO_RETRY_ALLOWED.name(), PaymentResultState.INTERNAL_ERROR,
                PaymentCreateResult.CREATE_DB_FAILED_OTHER.name(), PaymentResultState.INTERNAL_ERROR);
        /*
            According to mapping there are errors which we treat as success like UPDATE_DB_FAILED_TIMEOUT
            The payment considered as PROCESSED in this case
         */
    }

    public PaymentResultDto resolve(PaymentRuntimeResult paymentRuntimeResult) {

        return PaymentResultDto.builder()
                .paymentDate(paymentRuntimeResult.getPaymentDto().getPaymentDate())
                .key(paymentRuntimeResult.getPaymentDto().getKey())
                .amount(paymentRuntimeResult.getPaymentDto().getAmount())
                .paymentResultState(map(paymentRuntimeResult))
                .build();
    }

    private PaymentResultState map(PaymentRuntimeResult paymentRuntimeResult) {

        return Stream.of(paymentRuntimeResult.getPaymentCreateResult(),
                        paymentRuntimeResult.getPaymentSendResult())
                .map(Enum::name)
                .map(states::get)
                .findFirst()
                .orElse(PaymentResultState.PROCESSED);
    }
}
