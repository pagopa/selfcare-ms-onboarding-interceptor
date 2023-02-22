package it.pagopa.selfcare.onobarding.interceptor.connector.dao.model;

import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import lombok.Data;

import java.time.Instant;

@Data
public class PendingOnboardingNotificationOperationsDummy implements PendingOnboardingNotificationOperations {
    private String id;
    private Instant createdAt;

    private AutoApprovalOnboardingRequest request;

    private InstitutionOnboardedNotification notification;
}
