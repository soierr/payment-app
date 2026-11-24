package payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import payment.model.PaymentResultState;

import java.math.BigDecimal;
import java.time.Instant;


/*
    Here we return some important attributes, projection of PaymentDto
    For the API client to identify a payment in case of any problems
 */
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class PaymentResultDto {
    private final String key;
    private final Instant paymentDate;
    private final BigDecimal amount;
    private final String partner;
    private final PaymentResultState paymentResultState;
}
