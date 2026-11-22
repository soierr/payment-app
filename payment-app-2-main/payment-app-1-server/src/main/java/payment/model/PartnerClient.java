package payment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Builder
@RequiredArgsConstructor
@Getter
public class PartnerClient {
    private final RestClient restClient;
    private final String uri;
    private final boolean idempotent;
}
