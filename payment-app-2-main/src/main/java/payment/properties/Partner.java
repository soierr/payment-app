package payment.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class Partner {
    private String paymentUrl;
    private boolean idempotent;
}
