package payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import payment.dto.PaymentResultDto;
import payment.model.PaymentResultState;
import payment.service.PaymentProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest
class PaymentControllerComponentTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentProcessor paymentProcessor;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testKeyNull() throws Exception {

        String paymentDto = """
                {
                  "key": null,
                  "paymentDate": "2026-11-19T10:00:00.123Z",
                  "amount": 100.00,
                  "partner": "partner-1"
                }""";

        /*
            Expected response, ProblemDetails
            {
                "detail": "Validation failed, field 'key': rejected value [null]",
                "instance": "/payment",
                "status": 400,
                "title": "Bad Request"
            }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed, field 'key': rejected value [null]"))
                .andExpect(jsonPath("$.instance").value("/payment"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void testPartnerNull() throws Exception {

        String paymentDto = """
                {
                  "key": "key-1",
                  "paymentDate": "2026-11-19T10:00:00.123Z",
                  "amount": 100.00,
                  "partner": null
                }""";

        /*
            Expected response, ProblemDetails
            {
                "detail": "Validation failed, field 'partner': rejected value [null]",
                "instance": "/payment",
                "status": 400,
                "title": "Bad Request"
            }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed, field 'partner': rejected value [null]"))
                .andExpect(jsonPath("$.instance").value("/payment"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void testPartnerEmpty() throws Exception {

        String paymentDto = """
                {
                  "key": "key-1",
                  "paymentDate": "2026-11-19T10:00:00.123Z",
                  "amount": 100.00,
                  "partner": ""
                }""";

        /*
            Expected response, ProblemDetails
            {
                "detail": "Validation failed, field 'partner': rejected value []",
                "instance": "/payment",
                "status": 400,
                "title": "Bad Request"
            }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed, field 'partner': rejected value []"))
                .andExpect(jsonPath("$.instance").value("/payment"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void testMalformedJson() throws Exception {

        String paymentDto = """
                
                  "key": null,
                  "paymentDate": "2026-11-19T10:00:00.123Z",
                  "amount": 100.00
                  "partner": "partner-1"
                }""";

        /*
            Expected response, ProblemDetails
            {
                "detail": "JSON parse error",
                "instance": "/payment",
                "status": 400,
                "title": "Bad Request"
            }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("JSON parse error"))
                .andExpect(jsonPath("$.instance").value("/payment"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void testProperPaymentResultDtoReturned() throws Exception {

        String paymentDto = """
                {
                    "key": "key-1",
                    "paymentDate": "2026-11-19T10:00:00.123Z",
                    "amount": "10.00",
                    "partner": "partner-1"
                }""";

        PaymentResultDto paymentResultDto = PaymentResultDto.builder()
                .key("key-1")
                .paymentDate(Instant.parse("2026-11-19T10:00:00.123Z"))
                .amount(new BigDecimal("10.00"))
                .paymentResultState(PaymentResultState.PROCESSED)
                .partner("partner-1")
                .build();

        when(paymentProcessor.process(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenReturn(paymentResultDto);

        /*
            Expected response, PaymentResultDto
                {
                    "key": "key-1",
                    "paymentDate": "2026-11-19T10:00:00.123Z",
                    "amount": 10.00,
                    "paymentResultState": "PROCESSED",
                    "partner": "partner-1"
               }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.key").value("key-1"))
                .andExpect(jsonPath("$.paymentDate").value("2026-11-19T10:00:00.123Z"))
                .andExpect(jsonPath("$.amount").value(10.00)) //doesn't matter how many trailing zeros, strange assert
                .andExpect(jsonPath("$.paymentResultState").value("PROCESSED"))
                .andExpect(jsonPath("$.partner").value("partner-1"));
    }

    @Test
    void testPaymentProcessorCalledProperly() throws Exception {

        String paymentDto = """
                {
                    "key": "key-1",
                    "paymentDate": "2026-11-19T10:00:00.123Z",
                    "amount": "10.00",
                    "partner": "partner-1"
                }""";

        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentDto));

        verify(paymentProcessor, times(1)).process(argThat(p -> {

            assertThat(p.getKey()).isEqualTo("key-1");
            assertThat(p.getPaymentDate()).isEqualTo(Instant.parse("2026-11-19T10:00:00.123Z"));
            assertThat(p.getAmount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(p.getPartner()).isEqualTo("partner-1");
            return true;
        }));
    }

    @Test
    void test() throws Exception {

        String paymentDto = """
                {
                    "key": "key-1",
                    "paymentDate": "2026-11-19T10:00:00.123Z",
                    "amount": "10.00",
                    "partner": "partner-1"
                }""";

        when(paymentProcessor.process(argThat(p -> {
            assertThat(p.getKey()).isEqualTo("key-1");
            return true;
        }))).thenThrow(new ConversionNotSupportedException(new Object(), null, new RuntimeException("This test cause")));

        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentDto));

        /*
            Expected response, ProblemDetails
            {
                "detail": "Internal Error",
                "instance": "/payment",
                "status": 500,
                "title": "Internal Server Error"
            }
         */
        mockMvc.perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Internal Error"))
                .andExpect(jsonPath("$.instance").value("/payment"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }
}