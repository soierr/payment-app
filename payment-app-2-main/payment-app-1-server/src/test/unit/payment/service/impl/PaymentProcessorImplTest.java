package payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import payment.dto.PaymentDto;
import payment.model.Payment;
import payment.model.PaymentCreateResult;
import payment.model.PaymentRuntimeResult;
import payment.model.PaymentSendResult;
import payment.model.PaymentUpdateResult;
import payment.service.PartnerService;
import payment.service.PaymentRecoveryService;
import payment.service.PaymentResultResolver;
import payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorImplTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private PartnerService partnerService;
    @Mock
    private PaymentRecoveryService paymentRecoveryService;
    @Mock
    private PaymentResultResolver paymentResultResolver;
    @InjectMocks
    private PaymentProcessorImpl paymentProcessor;

    @Test
    void testProcessPaymentRetrievedIsNotNull() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        Payment payment = new Payment();

        payment.setKey("key-1");
        payment.setPaymentDate(paymentDate);
        payment.setAmount(new BigDecimal("10.00"));
        payment.setPartner("partner-1");
        payment.setPaymentSendResult(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);

        when(paymentService.findByKey("key-1")).thenReturn(payment);

        paymentProcessor.process(paymentDto);

        ArgumentCaptor<PaymentRuntimeResult> paymentRuntimeResultCaptor = ArgumentCaptor.forClass(PaymentRuntimeResult.class);

        verify(paymentRecoveryService, times(1)).process(paymentRuntimeResultCaptor.capture());

        PaymentRuntimeResult paymentRuntimeResultActual = paymentRuntimeResultCaptor.getValue();

        assertThat(paymentRuntimeResultActual.getPaymentSendResult()).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);
        assertThat(paymentRuntimeResultActual.getPaymentCreateResult()).isNull();
        assertThat(paymentRuntimeResultActual.getPaymentUpdateResult()).isNull();

        assertThat(paymentRuntimeResultActual.getPaymentDto().getKey()).isEqualTo(paymentDto.getKey());
        assertThat(paymentRuntimeResultActual.getPaymentDto().getPaymentDate()).isEqualTo(paymentDto.getPaymentDate());
        assertThat(paymentRuntimeResultActual.getPaymentDto().getAmount()).isEqualTo(paymentDto.getAmount());
        assertThat(paymentRuntimeResultActual.getPaymentDto().getPartner()).isEqualTo(paymentDto.getPartner());
    }

    @Test
    void testPaymentResultResolverIsProperlyCalled() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        Payment payment = new Payment();

        payment.setKey("key-1");
        payment.setPaymentDate(paymentDate);
        payment.setAmount(new BigDecimal("11.00")); //for example client sends retry request but with another payment details, key is the same, we process with new data, probably should throw an exception
        payment.setPartner("partner-1");
        payment.setPaymentSendResult(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);

        when(paymentService.findByKey("key-1")).thenReturn(payment);

        PaymentRuntimeResult paymentRuntimeResultUpdated = PaymentRuntimeResult.builder()
                .paymentSendResult(PaymentSendResult.SENT_TO_PARTNER_SUCCESS)
                .build();

        when(paymentRecoveryService.process(argThat(result -> {
            assertThat(result.getPaymentDto().getKey()).isEqualTo("key-1");
            assertThat(result.getPaymentSendResult()).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT);
            return true;
        }))).thenReturn(paymentRuntimeResultUpdated);

        paymentProcessor.process(paymentDto);

        ArgumentCaptor<PaymentRuntimeResult> paymentRuntimeResultCaptor = ArgumentCaptor.forClass(PaymentRuntimeResult.class);

        verify(paymentResultResolver, times(1)).resolve(paymentRuntimeResultCaptor.capture());

        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentSendResult()).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);
    }

    @Test
    void testCreatePaymentServiceIsProperlyCalled() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentService.findByKey("key-1")).thenReturn(null); //null is returned by mock by default afaik, added for clarity

        ArgumentCaptor<PaymentDto> paymentDtoCaptor = ArgumentCaptor.forClass(PaymentDto.class);

        paymentProcessor.process(paymentDto);

        verify(paymentService, times(1)).create(paymentDtoCaptor.capture());

        assertThat(paymentDtoCaptor.getValue().getKey()).isEqualTo(paymentDto.getKey());
        assertThat(paymentDtoCaptor.getValue().getPaymentDate()).isEqualTo(paymentDto.getPaymentDate());
        assertThat(paymentDtoCaptor.getValue().getAmount()).isEqualTo(paymentDto.getAmount());
        assertThat(paymentDtoCaptor.getValue().getPartner()).isEqualTo(paymentDto.getPartner());
    }

    @Test
    void testCreatedPaymentInDbFailed() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentService.create(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentCreateResult.CREATE_DB_FAILED_TIMEOUT);

        ArgumentCaptor<PaymentRuntimeResult> paymentRuntimeResultCaptor = ArgumentCaptor.forClass(PaymentRuntimeResult.class);

        paymentProcessor.process(paymentDto);

        verify(paymentResultResolver, times(1)).resolve(paymentRuntimeResultCaptor.capture());

        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentDto())
                .usingRecursiveComparison()
                .comparingOnlyFields("key", "paymentDate", "amount", "partner") //cool feature
                .isEqualTo(paymentDto);

        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentCreateResult())
                .isEqualTo(PaymentCreateResult.CREATE_DB_FAILED_TIMEOUT);
    }

    @Test
    void testNewPaymentSentToPartner() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentService.create(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentCreateResult.CREATE_DB_SUCCESS);

        ArgumentCaptor<PaymentDto> paymentDtoCaptor = ArgumentCaptor.forClass(PaymentDto.class);

        paymentProcessor.process(paymentDto);

        verify(partnerService, times(1)).send(paymentDtoCaptor.capture());

        assertThat(paymentDtoCaptor.getValue())
                .usingRecursiveComparison()
                .comparingOnlyFields("key", "paymentDate", "amount", "partner") //cool feature
                .isEqualTo(paymentDto);
    }

    @Test
    void testNewPaymentAfterSendProperlyUpdated() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentService.create(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentCreateResult.CREATE_DB_SUCCESS);

        when(partnerService.send(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);

        ArgumentCaptor<PaymentDto> paymentDtoCaptor = ArgumentCaptor.forClass(PaymentDto.class);

        paymentProcessor.process(paymentDto);

        verify(paymentService, times(1)).update(paymentDtoCaptor.capture(),
                eq(PaymentSendResult.SENT_TO_PARTNER_SUCCESS));

        assertThat(paymentDtoCaptor.getValue())
                .usingRecursiveComparison()
                .comparingOnlyFields("key", "paymentDate", "amount", "partner") //cool feature
                .isEqualTo(paymentDto);
    }

    @Test
    void testResolverIsCalledProperlyAtNewPayment() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentService.create(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentCreateResult.CREATE_DB_SUCCESS);

        when(partnerService.send(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);

        when(paymentService.update(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }), eq(PaymentSendResult.SENT_TO_PARTNER_SUCCESS))).thenReturn(PaymentUpdateResult.UPDATE_DB_SUCCESS);

        ArgumentCaptor<PaymentRuntimeResult> paymentRuntimeResultCaptor = ArgumentCaptor.forClass(PaymentRuntimeResult.class);

        paymentProcessor.process(paymentDto);

        verify(paymentResultResolver, times(1)).resolve(paymentRuntimeResultCaptor.capture());

        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentDto())
                .usingRecursiveComparison()
                .comparingOnlyFields("key", "paymentDate", "amount", "partner") //cool feature
                .isEqualTo(paymentDto);

        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentCreateResult()).isEqualTo(PaymentCreateResult.CREATE_DB_SUCCESS);
        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentSendResult()).isEqualTo(PaymentSendResult.SENT_TO_PARTNER_SUCCESS);
        assertThat(paymentRuntimeResultCaptor.getValue().getPaymentUpdateResult()).isEqualTo(PaymentUpdateResult.UPDATE_DB_SUCCESS);
    }

}