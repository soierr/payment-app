package payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import payment.dto.PaymentDto;
import payment.model.PaymentSendResult;
import payment.service.PartnerService;

@Service
@Slf4j
public class PartnerServiceImpl implements PartnerService {


    private final RestClient restClient;

    public PartnerServiceImpl(@Qualifier("restClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PaymentSendResult send(PaymentDto paymentDto) {

        try {

            restClient.post()
                    .uri("/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentDto)
                    .retrieve()
                    .toBodilessEntity();

            //make sure 200 received
            return PaymentSendResult.SENT_TO_PARTNER_SUCCESS;

        } catch (RuntimeException e) {

            log.error("Payment sending failed", e);

            //map error here: recoverable/non-recoverable
            return PaymentSendResult.SENT_TO_PARTNER_FAILED_OTHER;
        }

    }
}
