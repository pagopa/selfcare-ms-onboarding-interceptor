package it.pagopa.selfcare.onboarding.interceptor.model.kafka;


import lombok.Data;

@Data
public class InstitutionOnboardedNotification {

    private String id;
    private String internalIstitutionID;
    private String product;
    private String state;
    private String filePath;
    private String fileName;
    private String contentType;
    private String onboardingTokenId;
    private String pricingPlan;
    private InstitutionOnboarded institution;
    private InstitutionOnboardedBilling billing;
    private String  updatedAt;
    private String createdAt;
    private String closedAt;
}
