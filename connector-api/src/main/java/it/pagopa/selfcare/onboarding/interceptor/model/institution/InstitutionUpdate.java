package it.pagopa.selfcare.onboarding.interceptor.model.institution;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;
    private String zipCode;
    private List<String> geographicTaxonomyCodes;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private Boolean imported;

}
