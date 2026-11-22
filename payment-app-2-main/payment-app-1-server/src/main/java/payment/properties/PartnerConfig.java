package payment.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnerConfig {
    private String baseUrl;
    private String uri;
    private boolean idempotent;
    private int connectionTimeoutMillis;
    private int readTimeoutMillis;
}
