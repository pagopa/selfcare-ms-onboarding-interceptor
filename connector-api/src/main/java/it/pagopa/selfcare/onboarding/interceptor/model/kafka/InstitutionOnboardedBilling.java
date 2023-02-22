package it.pagopa.selfcare.onboarding.interceptor.model.kafka;

import lombok.Data;

@Data
public class InstitutionOnboardedBilling {

    private String vatNumber;
    private String recipientCode;
    private boolean publicService;

}
