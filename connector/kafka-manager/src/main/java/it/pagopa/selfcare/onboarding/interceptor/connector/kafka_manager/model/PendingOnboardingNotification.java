package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;

import it.pagopa.selfcare.onboarding.interceptor.model.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import lombok.Data;

import java.time.Instant;

@Data
public class PendingOnboardingNotification implements PendingOnboardingNotificationOperations {
    private String id;
    private Instant createdAt;

    private AutoApprovalOnboardingRequest request;

    private InstitutionOnboardedNotification notification;

    private String onboardingFailure;
}
