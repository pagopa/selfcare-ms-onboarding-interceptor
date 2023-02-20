package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;


import lombok.Data;

import java.time.OffsetDateTime;

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
    private OffsetDateTime updatedAt;
}
