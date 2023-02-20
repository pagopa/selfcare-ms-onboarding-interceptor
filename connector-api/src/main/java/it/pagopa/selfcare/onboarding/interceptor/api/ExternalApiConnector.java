package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;

public interface ExternalApiConnector {
    void autoApprovalOnboarding(String externalInstitutionId, String productId, AutoApprovalOnboardingRequest request);

}
