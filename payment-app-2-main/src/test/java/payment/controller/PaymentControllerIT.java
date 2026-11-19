package payment.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;
import payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = PaymentControllerIT.TestCtx.class)
class PaymentControllerIT {

    private final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8083));

    @Autowired
    RestClient testRestClient;

    @Test
    void testCreatePayment() {

        ResponseDefinitionBuilder successBlankResponse = WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json");

        MappingBuilder paymentPostMapper = WireMock.post("/payment")
                .withHeader(HttpHeaders.CONTENT_TYPE, matching("application/json"))
                .withRequestBody(matching(".*"))
                .willReturn(successBlankResponse);

        wireMockServer.start();

        wireMockServer.stubFor(paymentPostMapper);

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setMerchant("merchant-1");

        testRestClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentDto)
                .retrieve()
                .toBodilessEntity();
    }

    @TestConfiguration
    static class TestCtx {

        @Bean
        RestClient testRestClient() {
            return RestClient.builder()
                    .baseUrl("http://localhost:8082")
                    .build();
        }
    }
}