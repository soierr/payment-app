package payment.service;

import payment.model.Partner;
import payment.model.PartnerClient;

public interface PartnerClientManager {

    PartnerClient get(String partner);
}
