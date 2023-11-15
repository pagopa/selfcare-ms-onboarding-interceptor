package it.pagopa.selfcare.onboarding.interceptor.model.kafka;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.RootParent;
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
    private String zipCode;
    private String istatCode;
    private String city;
    private String country;
    private String county;
    private String subUnitCode;
    private String category;
    private String subUnitType;
    private RootParent rootParent;
}
