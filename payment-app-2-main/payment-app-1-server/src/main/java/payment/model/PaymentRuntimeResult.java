package payment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import payment.dto.PaymentDto;

@RequiredArgsConstructor
@Builder
@Getter
public class PaymentRuntimeResult {
    private final PaymentDto paymentDto;
    private final PaymentCreateResult paymentCreateResult;
    private final PaymentSendResult paymentSendResult;
    private final PaymentUpdateResult paymentUpdateResult;
}
