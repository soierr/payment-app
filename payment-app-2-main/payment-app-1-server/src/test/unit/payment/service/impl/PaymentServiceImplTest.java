package payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import payment.dto.PaymentDto;
import payment.model.Payment;
import payment.model.PaymentCreateResult;
import payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    PaymentServiceImpl paymentService;

    @Test
    void testCreatPayment() {

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

        PaymentCreateResult paymentCreateResult = paymentService.create(paymentDto);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository, times(1)).save(paymentCaptor.capture());

        assertThat(paymentCaptor.getValue())
                .usingRecursiveComparison()
                .comparingOnlyFields("key", "paymentDate", "amount", "partner")
                .isEqualTo(payment);

        assertThat(paymentCreateResult).isEqualTo(PaymentCreateResult.CREATE_DB_SUCCESS);
    }

    @Test
    void testCreatPaymentExceptionIsThrown() {

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

        when(paymentRepository.save(any(Payment.class))).thenThrow(new ResourceAccessException("Constraint violation"));

        assertThatCode(() -> paymentService.create(paymentDto)).doesNotThrowAnyException();
    }

    @Test
    void testProperResultWhenExceptionThrown() {

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

        when(paymentRepository.save(any(Payment.class))).thenThrow(new ResourceAccessException("Constraint violation"));

        PaymentCreateResult paymentCreateResult = paymentService.create(paymentDto);

        assertThat(paymentCreateResult).isEqualTo(PaymentCreateResult.CREATE_DB_FAILED_OTHER);
    }
}