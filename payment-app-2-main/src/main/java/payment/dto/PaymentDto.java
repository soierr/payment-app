package payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

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
    private String merchant;
}
