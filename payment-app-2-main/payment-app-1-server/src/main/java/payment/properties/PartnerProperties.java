package payment.properties;

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
    private Map<String, PartnerConfig> partners;
}
