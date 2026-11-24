package payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment.dto.PaymentDto;
import payment.dto.PaymentResultDto;
import payment.service.PaymentProcessor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    /*
        Responsibility for the controller here is to only validate dto
        and pass it for further processing
     */

    private final PaymentProcessor paymentProcessor;
    private final ObjectMapper objectMapper;

    @PostMapping
    PaymentResultDto createPayment(@Validated @RequestBody PaymentDto paymentDto) {
        return paymentProcessor.process(paymentDto);
    }
}
