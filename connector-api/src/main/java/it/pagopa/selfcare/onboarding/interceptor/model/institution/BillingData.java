package it.pagopa.selfcare.onboarding.interceptor.model.institution;


import lombok.Data;

@Data
public class BillingData {

    private String businessName;
    private String registeredOffice;
    private String digitalAddress;
    private String zipCode;
    private String taxCode;
    private String vatNumber;
    private String recipientCode;
    private Boolean publicServices;

}
