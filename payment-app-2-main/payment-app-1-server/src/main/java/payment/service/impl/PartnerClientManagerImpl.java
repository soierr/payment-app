package payment.service.impl;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import payment.model.Partner;
import payment.model.PartnerClient;
import payment.properties.PartnerConfig;
import payment.properties.PartnerProperties;
import payment.service.PartnerClientManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PartnerClientManagerImpl implements PartnerClientManager {

    private final Map<String, PartnerClient> partnerClients;

    public PartnerClientManagerImpl(PartnerProperties partnerProperties) {
        partnerClients = partnerProperties.getPartners()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> partnerClient(e.getValue())));
    }

    @Override
    public PartnerClient get(String partner) {
        return partnerClients.get(partner);
    }

    private static PartnerClient partnerClient(PartnerConfig partnerConfig) {

        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.of(partnerConfig.getConnectionTimeoutMillis(), ChronoUnit.MILLIS))
                .withReadTimeout(Duration.of(partnerConfig.getReadTimeoutMillis(), ChronoUnit.MILLIS));


        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);

        RestClient restClient = RestClient.builder()
                .baseUrl(partnerConfig.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        return PartnerClient.builder()
                .idempotent(partnerConfig.isIdempotent())
                .uri(partnerConfig.getUri())
                .restClient(restClient)
                .build();
    }
}
