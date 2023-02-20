package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;

import java.util.List;

public interface ExternalApiConnector {
    void autoApprovalOnboarding(String externalInstitutionId, String productId, AutoApprovalOnboardingRequest request);

    Institution getInstitutionById(String institutionId);

    List<User> getInstitutionProductUsers(String institutionId, String productId);

}
