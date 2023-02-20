package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;

import lombok.Data;

@Data
public class InstitutionOnboardedBilling {

    private String vatNumber;
    private String recipientCode;
    private boolean publicService;

}
