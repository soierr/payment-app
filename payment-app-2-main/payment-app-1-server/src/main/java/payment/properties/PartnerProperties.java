package payment.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import payment.model.Partner;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "external")
@Getter
@Setter
@Validated
public class PartnerProperties {
    Map<Partner, PartnerConfig> partners;
}
