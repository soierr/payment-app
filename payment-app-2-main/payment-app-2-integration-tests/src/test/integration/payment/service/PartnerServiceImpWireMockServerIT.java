
package payment.service;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import payment.dto.PaymentDto;
import payment.model.Partner;
import payment.model.PaymentSendResult;
import payment.repository.PaymentRepository;
import payment.service.impl.PartnerServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(PartnerServiceImpl.class)
@TestPropertySource(properties = {
    "external.partners.partner-1.connection-timeout-millis=10000",
    "external.partners.partner-1.read-timeout-millis=1000",
})
@Disabled("Left as an example of wiremock configuration")
class PartnerServiceImpWireMockServerIT {

    @Autowired
    PartnerServiceImpl partnerService;

    @MockitoBean
    PaymentRepository paymentRepository;

    private static final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8083));

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
    }

    @Test
    void testReadTimeout() {

        ResponseDefinitionBuilder successBlankResponse = WireMock.aResponse()
                .withStatus(200)
                .withFixedDelay(5000)
                .withHeader("Content-Type", "application/json");

        MappingBuilder paymentPostMapper = WireMock.post("/payment")
                .withHeader(HttpHeaders.CONTENT_TYPE, matching("application/json"))
                .withRequestBody(matching(".*"))
                .willReturn(successBlankResponse);

        wireMockServer.stubFor(paymentPostMapper);

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner("partner-1");

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }
}