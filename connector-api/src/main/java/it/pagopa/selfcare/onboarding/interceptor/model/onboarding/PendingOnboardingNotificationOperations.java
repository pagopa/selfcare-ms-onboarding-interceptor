package it.pagopa.selfcare.onboarding.interceptor.model.onboarding;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;

import java.time.Instant;

public interface PendingOnboardingNotificationOperations {
    String getId();

    void setId(String id);

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

    OnboardingProductRequest getRequest();

    void setRequest(OnboardingProductRequest request);

    InstitutionOnboardedNotification getNotification();

    void setNotification(InstitutionOnboardedNotification notification);

    String getOnboardingFailure();

    void setOnboardingFailure(String error);

    Instant getModifiedAt();

    void setModifiedAt(Instant modifiedAt);
}
