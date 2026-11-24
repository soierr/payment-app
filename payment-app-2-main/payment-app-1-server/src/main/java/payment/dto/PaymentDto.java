package payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import payment.model.Partner;
import tools.jackson.databind.EnumNamingStrategy;
import tools.jackson.databind.annotation.EnumNaming;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class PaymentDto {

    @NotBlank
    private String key;
    @NotNull
    private Instant paymentDate;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String partner;
}
