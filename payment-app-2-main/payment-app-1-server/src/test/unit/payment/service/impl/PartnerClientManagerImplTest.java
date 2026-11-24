package payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import payment.model.PartnerClient;
import payment.properties.PartnerConfig;
import payment.properties.PartnerProperties;
import payment.service.PartnerClientManager;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class PartnerClientManagerImplTest {


    @Test
    void testProperPartnerPropertyReturned() {

        PartnerConfig partner1Config = new PartnerConfig();
        partner1Config.setBaseUrl("http://localhost:8080");
        partner1Config.setUri("/bank-1/payment");
        partner1Config.setIdempotent(true);
        partner1Config.setConnectionTimeoutMillis(3000);
        partner1Config.setReadTimeoutMillis(4000);

        PartnerConfig partner2Config = new PartnerConfig();
        partner2Config.setBaseUrl("http://localhost:8082");
        partner2Config.setUri("/bank-2/payment");
        partner2Config.setIdempotent(false);
        partner2Config.setConnectionTimeoutMillis(6000);
        partner2Config.setReadTimeoutMillis(7000);

        PartnerProperties partnerProperties = new PartnerProperties();

        partnerProperties.setPartners(Map.of("partner-1", partner1Config, "partner-2", partner2Config));

        PartnerClientManagerImpl partnerClientManager = new PartnerClientManagerImpl(partnerProperties);

        PartnerClient partner1Client = partnerClientManager.get("partner-1");
        PartnerClient partner2Client = partnerClientManager.get("partner-2");

        assertThat(partner1Client.getRestClient()).isNotNull();
        assertThat(partner1Client.getUri()).isEqualTo("/bank-1/payment");
        assertThat(partner1Client.isIdempotent()).isTrue();

        assertThat(partner2Client.getRestClient()).isNotNull();
        assertThat(partner2Client.getUri()).isEqualTo("/bank-2/payment");
        assertThat(partner2Client.isIdempotent()).isFalse();

    }

}