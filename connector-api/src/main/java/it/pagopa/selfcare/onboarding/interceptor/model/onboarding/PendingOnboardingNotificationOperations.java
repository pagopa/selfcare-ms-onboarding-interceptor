package it.pagopa.selfcare.onboarding.interceptor.model.onboarding;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;

import java.time.Instant;

public interface PendingOnboardingNotificationOperations {
    String getId();

    void setId(String id);

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

    AutoApprovalOnboardingRequest getRequest();

    void setRequest(AutoApprovalOnboardingRequest request);

    InstitutionOnboardedNotification getNotification();

    void setNotification(InstitutionOnboardedNotification notification);

    String getOnboardingFailure();

    void setOnboardingFailure(String error);
}
