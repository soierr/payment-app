package payment.ctx;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import payment.properties.PartnerProperties;

@Configuration
public class Ctx {

    @Bean
    RestClient restClient(PartnerProperties partnerProperties) {

        String merchantUrl = partnerProperties.getPartners().get("merchant_1").getPaymentUrl();

        return RestClient.builder()
                .baseUrl(merchantUrl)
                .build();
    }

}
