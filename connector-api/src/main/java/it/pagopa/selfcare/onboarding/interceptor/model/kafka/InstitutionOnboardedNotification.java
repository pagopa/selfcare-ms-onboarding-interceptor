package it.pagopa.selfcare.onboarding.interceptor.model.kafka;


import it.pagopa.selfcare.onboarding.interceptor.model.institution.Billing;
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
    private InstitutionToNotify institution;
    private Billing billing;
    private OffsetDateTime createdAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime updatedAt;
    private QueueEvent notificationType;

}
