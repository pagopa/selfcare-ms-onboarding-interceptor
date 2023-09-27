package it.pagopa.selfcare.onboarding.interceptor.model.kafka;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
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
