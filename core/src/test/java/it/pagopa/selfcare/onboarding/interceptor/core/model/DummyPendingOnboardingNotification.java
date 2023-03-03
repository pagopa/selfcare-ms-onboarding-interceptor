package it.pagopa.selfcare.onboarding.interceptor.core.model;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.Data;

import java.time.Instant;

@Data
public class DummyPendingOnboardingNotification implements PendingOnboardingNotificationOperations {
    private String id;

    private Instant createdAt;

    private Instant modifiedAt;

    private AutoApprovalOnboardingRequest request;

    private InstitutionOnboardedNotification notification;

    private String onboardingFailure;
}
