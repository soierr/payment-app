package payment.service.impl;

import org.springframework.stereotype.Service;
import payment.model.PaymentRuntimeResult;
import payment.service.PaymentRecoveryService;

@Service
public class PaymentRecoveryServiceImpl implements PaymentRecoveryService {

    @Override
    public PaymentRuntimeResult process(PaymentRuntimeResult paymentRuntimeResult) {

        /*
            It can be idempotent or not, depending on merchant bank
            we decide what to do here
         */

        /*
            Log sent result might fail to store in db
         */

        /*
            Partner services idempotent as well
            so we are safe to call it one more time with the same payment
            in case we had received timeout at first call
         */
        return null;
    }
}
