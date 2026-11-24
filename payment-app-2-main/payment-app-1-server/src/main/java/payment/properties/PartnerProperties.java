package payment.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "external")
@Getter
@Setter
@Validated
public class PartnerProperties {
    @NotNull
    @NotEmpty
    private Map<String, PartnerConfig> partners;
}
