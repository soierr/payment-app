package payment.service.impl;

import org.springframework.stereotype.Component;
import payment.dto.PaymentResultDto;
import payment.model.PaymentRuntimeResult;
import payment.service.PaymentResultResolver;

@Component
public class PaymentResultResolverImpl implements PaymentResultResolver {

    public PaymentResultDto resolve(PaymentRuntimeResult paymentRuntimeResult) {

        return null;

        /*return PaymentResultDto.builder()
                .paymentDate(paymentResult.getPaymentDate())
                .key(paymentResult.getKey())
                .amount(paymentResult.getAmount())
                .paymentResultState(map(paymentResult.getPaymentResultState()))
                .build();*/
    }

    /*private PaymentResultState map(PaymentResultStateInternal paymentResultInternalState) {

        return switch (paymentResultInternalState) {
            case UPDATE_DB_FAILED -> PaymentResultState.INTERNAL_ERROR;
        };

    }*/
}
