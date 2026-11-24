package payment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import payment.ctx.TestContainerNginx;
import payment.ctx.TestContainersCtx;
import payment.dto.PaymentDto;
import payment.model.PartnerClient;
import payment.model.PaymentSendResult;
import payment.repository.PaymentRepository;
import payment.service.impl.PartnerServiceImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = PartnerServiceImplIT.TestConfig.class)
class PartnerServiceImplIT {

    @Autowired
    PartnerServiceImpl partnerService;

    @MockitoBean
    PaymentRepository paymentRepository;

    @MockitoBean
    PartnerClientManager partnerClientManager;

    @Autowired
    TestContainerNginx testContainerNginx;

    /*
        We have the following set up in play-nginx container, depending on port:
        8080 - Read timeout is available within 120s
        8082 - Connection request timeout, no time limit, connection is not established
        8084 - 504 Gateway time-out from nginx, after 2s
        See test/resources/nginx0-default.conf for details
     */

    @Test
    void testReadTimeout() {

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner("partner-1");

        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.of(20_000, ChronoUnit.MILLIS))
                .withReadTimeout(Duration.of(1000, ChronoUnit.MILLIS));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);

        PartnerClient partnerClient = PartnerClient.builder()
                .restClient(RestClient.builder()
                        .requestFactory(requestFactory)
                        .baseUrl("http://" + testContainerNginx.getIp() + ":8080")
                        .build())
                .uri("/payment")
                .idempotent(true)
                .build();

        when(partnerClientManager.get(anyString())).thenReturn(partnerClient);

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);


        /*
            org.springframework.web.client.ResourceAccessException
            Caused by: java.net.http.HttpTimeoutException
         */
        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @Test
    void testConnectionTimeout() {

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner("partner-1");

        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.of(1000, ChronoUnit.MILLIS))
                .withReadTimeout(Duration.of(20_000, ChronoUnit.MILLIS));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);

        PartnerClient partnerClient = PartnerClient.builder()
                .restClient(RestClient.builder()
                        .requestFactory(requestFactory)
                        .baseUrl("http://" + testContainerNginx.getIp() + ":8082")
                        .build())
                .uri("/payment")
                .idempotent(true)
                .build();

        when(partnerClientManager.get(anyString())).thenReturn(partnerClient);

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        /*
            org.springframework.web.client.ResourceAccessException
            Caused by: java.net.http.HttpConnectTimeoutException
         */
        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @Test
    void test504GatewayTimeout() {

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner("partner-1");

        HttpClientSettings httpClientSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.of(3000, ChronoUnit.MILLIS))
                .withReadTimeout(Duration.of(3000, ChronoUnit.MILLIS));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(httpClientSettings);

        PartnerClient partnerClient = PartnerClient.builder()
                .restClient(RestClient.builder()
                        .requestFactory(requestFactory)
                        .baseUrl("http://" + testContainerNginx.getIp() + ":8084")
                        .build())
                .uri("/payment")
                .idempotent(true)
                .build();

        when(partnerClientManager.get(anyString())).thenReturn(partnerClient);

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        /*
            org.springframework.web.client.HttpServerErrorException$GatewayTimeout
         */
        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @Import({PartnerServiceImpl.class, TestContainersCtx.class})
    static class TestConfig {

        /*
            Here we can override any bean, not in @TestConfiguration
         */
    }

}