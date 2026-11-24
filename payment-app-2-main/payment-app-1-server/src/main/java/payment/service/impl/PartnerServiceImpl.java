package payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import payment.dto.PaymentDto;
import payment.model.PartnerClient;
import payment.model.PaymentSendResult;
import payment.service.PartnerClientManager;
import payment.service.PartnerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    private static final String PAYMENT_SENDING_FAILED = "Payment sending failed";

    private final PartnerClientManager partnerClientManager;

    @Override
    public PaymentSendResult send(PaymentDto paymentDto) {

        PartnerClient partnerClient = partnerClientManager.get(paymentDto.getPartner());

        try {

            RestClient restClient = partnerClient.getRestClient();
            String uri = partnerClient.getUri();

            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentDto)
                    .retrieve()
                    .toBodilessEntity();

            //make sure 200 received
            return PaymentSendResult.SENT_TO_PARTNER_SUCCESS;

        } catch (ResourceAccessException | HttpServerErrorException.GatewayTimeout recoverableException){

            log.error(PAYMENT_SENDING_FAILED, recoverableException);

            return PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT;

        } catch (RuntimeException e) {

            log.error(PAYMENT_SENDING_FAILED, e);

            //map error here: recoverable/non-recoverable
            return PaymentSendResult.SENT_TO_PARTNER_FAILED_OTHER;
        }
    }
}
