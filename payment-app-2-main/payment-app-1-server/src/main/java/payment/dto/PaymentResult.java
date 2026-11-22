package payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;

import java.math.BigDecimal;


/*
    Here we return some important attributes
    for API client to identify the payment in case of any problems
 */
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class PaymentResult {
    private final String key;
    private final String paymentDate;
    private final BigDecimal amount;
    private final String merchant;
    private final PaymentSendResult paymentSendResult;
    private final PaymentUpdateResult paymentUpdateResult;
}
