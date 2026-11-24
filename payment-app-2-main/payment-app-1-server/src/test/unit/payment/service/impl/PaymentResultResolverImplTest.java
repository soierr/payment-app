package payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import payment.dto.PaymentDto;
import payment.dto.PaymentResultDto;
import payment.model.PaymentCreateResult;
import payment.model.PaymentResultState;
import payment.model.PaymentRuntimeResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentResultResolverImplTest {

    private final PaymentResultResolverImpl paymentResultResolver = new PaymentResultResolverImpl();

    @Test
    void testResolve() {

        PaymentRuntimeResult paymentRuntimeResult = PaymentRuntimeResult.builder()
                .paymentDto(new PaymentDto())
                .paymentCreateResult(PaymentCreateResult.CREATE_DB_FAILED_TIMEOUT)
                .build();

        PaymentResultDto paymentResultDto = paymentResultResolver.resolve(paymentRuntimeResult);

        assertThat(paymentResultDto.getPaymentResultState()).isEqualTo(PaymentResultState.TIMEOUT_ERROR);
    }
}