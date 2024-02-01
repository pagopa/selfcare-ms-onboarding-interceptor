package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;

import java.util.List;

public interface InternalApiConnector {
    void autoApprovalOnboarding(String externalInstitutionId, String productId, AutoApprovalOnboardingRequest request);

    Institution getInstitutionById(String institutionId);

    List<User> getInstitutionProductUsers(String institutionId, String productId);

    Product getProduct(String productId);

    void onboarding(OnboardingProductRequest request);

}
