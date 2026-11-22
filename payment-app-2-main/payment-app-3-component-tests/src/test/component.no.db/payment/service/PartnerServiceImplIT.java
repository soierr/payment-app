package payment.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import payment.dto.PaymentDto;
import payment.model.Partner;
import payment.model.PaymentSendResult;
import payment.repository.PaymentRepository;
import payment.service.impl.PartnerServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(PartnerServiceImpl.class)
class PartnerServiceImplIT {

    @Autowired
    PartnerServiceImpl partnerService;

    @MockitoBean
    PaymentRepository paymentRepository;

    private static ClientAndServer clientAndServer;

    @BeforeAll
    static void setUp() {

        Configuration configuration = new Configuration();
        configuration.logLevel(Level.ERROR);

        clientAndServer = ClientAndServer.startClientAndServer(configuration, 8083);

    }

    @Test
    void testConnectionTimeout() {


        clientAndServer.upsert(Expectation.when(HttpRequest.request().withPath("/bank-1/payment"))
                .thenRespond(HttpResponse.response()
                        .withStatusCode(200)));

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner(Partner.PARTNER_1);

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @Test
    void testReadTimeout() {

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-123");
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPaymentDate(Instant.now());
        paymentDto.setPartner(Partner.PARTNER_1);

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @AfterAll
    static void afterAll() {
        clientAndServer.stop();
    }
}