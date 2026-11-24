package payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import payment.dto.PaymentDto;
import payment.model.PartnerClient;
import payment.model.PaymentSendResult;
import payment.service.PartnerClientManager;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerServiceImplTest {

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodyUriSpec requestBodyUriSpecMock;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodySpec requestBodySpecMock;

    @Mock
    private RestClient.ResponseSpec responseSpecMock;

    @Mock
    private RestClient restClient;

    @Mock
    private PartnerClientManager partnerClientManager;

    @InjectMocks
    private PartnerServiceImpl partnerService;

    @Test
    void testSend() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);

        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);

        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);

        when(partnerClientManager.get("partner-1")).thenReturn(PartnerClient.builder()
                .restClient(restClient)
                .uri("/payment")
                .idempotent(true)
                .build());

        ArgumentCaptor<PaymentDto> paymentDtoCaptor = ArgumentCaptor.forClass(PaymentDto.class);

        partnerService.send(paymentDto);

        verify(restClient, times(1)).post();
        verify(requestBodyUriSpecMock, times(1)).uri("/payment");
        verify(requestBodySpecMock, times(1)).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpecMock, times(1)).body(paymentDtoCaptor.capture());
        verify(requestBodySpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).toBodilessEntity();

        PaymentDto paymentDtoActual = paymentDtoCaptor.getValue();

        assertThat(paymentDtoActual.getKey()).isEqualTo(paymentDto.getKey());
        assertThat(paymentDtoActual.getPaymentDate()).isEqualTo(paymentDto.getPaymentDate());
        assertThat(paymentDtoActual.getAmount()).isEqualTo(paymentDto.getAmount());
        assertThat(paymentDtoActual.getPartner()).isEqualTo(paymentDto.getPartner());
    }

    @Test
    void testSendPaymentExceptionIsNotPropagated() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);

        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);

        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);

        when(responseSpecMock.toBodilessEntity()).thenThrow(new ResourceAccessException("Read time out"));

        when(partnerClientManager.get("partner-1")).thenReturn(PartnerClient.builder()
                .restClient(restClient)
                .uri("/payment")
                .idempotent(true)
                .build());

        assertThatCode(() -> partnerService.send(paymentDto))
                .doesNotThrowAnyException();
    }

    @Test
    void testSendPaymentRecoverableExceptionIsThrown() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);

        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);

        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);

        when(responseSpecMock.toBodilessEntity()).thenThrow(new ResourceAccessException("Read time out"));

        when(partnerClientManager.get("partner-1")).thenReturn(PartnerClient.builder()
                .restClient(restClient)
                .uri("/payment")
                .idempotent(true)
                .build());

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
    }

    @Test
    void testSendPaymentNonRecoverableExceptionIsThrown() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);

        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);

        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);

        when(responseSpecMock.toBodilessEntity()).thenThrow(new IllegalArgumentException("No route found"));

        when(partnerClientManager.get("partner-1")).thenReturn(PartnerClient.builder()
                .restClient(restClient)
                .uri("/payment")
                .idempotent(true)
                .build());

        PaymentSendResult paymentSendResult = partnerService.send(paymentDto);

        assertThat(paymentSendResult).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_OTHER);
    }
}