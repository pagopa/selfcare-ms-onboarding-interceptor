package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.Data;

import java.time.Instant;

@Data
public class PendingOnboardingNotification implements PendingOnboardingNotificationOperations {

    private String id;

    private Instant createdAt;

    private Instant modifiedAt;

    private OnboardingProductRequest request;

    private InstitutionOnboardedNotification notification;

    private String onboardingFailure;
}
