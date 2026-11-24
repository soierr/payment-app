package payment.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import payment.dto.PaymentDto;
import payment.service.PaymentProcessor;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void testCreatePayment() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        paymentController.createPayment(paymentDto);

        verify(paymentProcessor).process(argThat(dto -> {

            assertThat(dto.getKey()).isEqualTo(paymentDto.getKey());
            assertThat(dto.getPaymentDate()).isEqualTo(paymentDto.getPaymentDate());
            assertThat(dto.getAmount()).isEqualTo(paymentDto.getAmount());
            assertThat(dto.getPartner()).isEqualTo(paymentDto.getPartner());

            return true;
        }));

    }

    @Test
    void testCreatePaymentExceptionIsThrown() {

        Instant paymentDate = Instant.now();

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setKey("key-1");
        paymentDto.setPaymentDate(paymentDate);
        paymentDto.setAmount(new BigDecimal("10.00"));
        paymentDto.setPartner("partner-1");

        when(paymentProcessor.process(any(PaymentDto.class))).thenThrow(new IllegalArgumentException("Test Exception"));

        assertThatThrownBy(() -> paymentController.createPayment(paymentDto))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test Exception");
    }

}