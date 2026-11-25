package payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import payment.dto.PaymentDto;
import payment.model.PartnerClient;
import payment.model.PaymentRuntimeResult;
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

            ResponseEntity<Void> res = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentDto)
                    .retrieve()
                    .toBodilessEntity();

            if (res.getStatusCode() != HttpStatusCode.valueOf(200)) { //other 20x possible depending on partner, left for simplicity

                log.error(MessageFormatter.format(PAYMENT_SENDING_FAILED + " with response code: {}", res.getStatusCode())
                        .getMessage());

                return PaymentSendResult.SENT_TO_PARTNER_FAILED_OTHER;
            }

            return PaymentSendResult.SENT_TO_PARTNER_SUCCESS;

        } catch (ResourceAccessException | HttpServerErrorException.GatewayTimeout recoverableException){

            log.error(PAYMENT_SENDING_FAILED, recoverableException);

            return partnerClient.isIdempotent() ?
                    PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT : PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT_NO_RETRY_ALLOWED;

        } catch (RuntimeException e) {

            log.error(PAYMENT_SENDING_FAILED, e);

            return PaymentSendResult.SENT_TO_PARTNER_FAILED_OTHER;
        }
    }

    @Override
    public PaymentSendResult sendRetry(PaymentRuntimeResult paymentRuntimeResult) {

        PaymentSendResult paymentSendResult = paymentRuntimeResult.getPaymentSendResult();

        PaymentDto paymentDto = paymentRuntimeResult.getPaymentDto();

        PartnerClient partnerClient = partnerClientManager.get(paymentDto.getPartner());

        if (paymentSendResult == PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT_NO_RETRY_ALLOWED) {
            return paymentSendResult;
        }

        if (paymentSendResult == null) {
            /*
                Looks like in previous call we saved payment, called partner or not, but failed to update record
                Now we need to double-check whether partner support idempotency/retry
             */

            if (partnerClient.isIdempotent()) {

                return send(paymentDto);
            } else {

                log.error(PaymentSendResult.SENT_TO_PARTNER_UNKNOWN_NO_RETRY_ALLOWED.name()); //to update

                /*
                    We don't know what happened before, db is failed when updating a record
                    I might be that sending has failed or succeeded
                    But we know for sure, that we can not call it twice
                 */
                return PaymentSendResult.SENT_TO_PARTNER_UNKNOWN_NO_RETRY_ALLOWED;
            }
        }

        if (paymentSendResult == PaymentSendResult.SENT_TO_PARTNER_FAILED_TIMEOUT) {
            return send(paymentDto);
        }

        return paymentSendResult;
    }
}
