package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.InstitutionType;
import lombok.Data;

@Data
public class InstitutionOnboarded {
    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;
    private String origin;
    private String originId;
}
