package it.pagopa.selfcare.onboarding.interceptor.connector.dao.model;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.Data;

import java.time.Instant;

@Data
public class PendingOnboardingNotificationOperationsDummy implements PendingOnboardingNotificationOperations {
    private String id;

    private Instant createdAt;

    private Instant modifiedAt;

    private OnboardingProductRequest request;

    private InstitutionOnboardedNotification notification;

    private String onboardingFailure;
}
