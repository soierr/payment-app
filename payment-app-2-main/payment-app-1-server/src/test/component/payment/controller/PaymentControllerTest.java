package payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import payment.service.PaymentProcessor;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebMvcTest
public class PaymentControllerTest {

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

        String expectedResponse = """
      {
        "detail": "Validation failed, field 'key': rejected value [null]",
        "instance": "/payment",
        "status": 400,
        "title": "Bad Request"
      }""";

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        assertThat(actualResponse).isEqualTo(objectMapper.readTree(expectedResponse).toString());
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

        String expectedResponse = """
      {
        "detail": "Validation failed, field 'partner': rejected value [null]",
        "instance": "/payment",
        "status": 400,
        "title": "Bad Request"
      }""";

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        assertThat(actualResponse).isEqualTo(objectMapper.readTree(expectedResponse).toString());
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

        String expectedResponse = """
      {
        "detail": "Validation failed, field 'partner': rejected value []",
        "instance": "/payment",
        "status": 400,
        "title": "Bad Request"
      }""";

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        assertThat(actualResponse).isEqualTo(objectMapper.readTree(expectedResponse).toString());
    }

    @Test
    void testMalformedJson() throws Exception {

        String paymentDto = """
      
              "key": null,
              "paymentDate": "2026-11-19T10:00:00.123Z",
              "amount": 100.00
              "partner": "partner-1"
            }""";

        String expectedResponse = """
      {
        "detail": "JSON parse error",
        "instance": "/payment",
        "status": 400,
        "title": "Bad Request"
      }""";

        MvcResult result  = mockMvc
                .perform(MockMvcRequestBuilders.post("/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        assertThat(actualResponse).isEqualTo(objectMapper.readTree(expectedResponse).toString());
    }
}