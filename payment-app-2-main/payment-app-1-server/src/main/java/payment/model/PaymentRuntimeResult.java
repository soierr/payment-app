package payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import payment.dto.PaymentDto;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentRuntimeResult {
    private PaymentDto paymentDto;
    private PaymentCreateResult paymentCreateResult;
    private PaymentSendResult paymentSendResult;
    private PaymentUpdateResult paymentUpdateResult;
}
