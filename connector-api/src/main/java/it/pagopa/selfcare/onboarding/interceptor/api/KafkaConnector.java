package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingInstitutionRequest;

public interface KafkaConnector<T> {

    OnboardingInstitutionRequest intercept(T message);
}
