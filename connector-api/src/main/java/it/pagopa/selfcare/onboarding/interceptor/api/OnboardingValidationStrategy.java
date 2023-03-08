package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface OnboardingValidationStrategy {

    boolean validate(InstitutionOnboardedNotification message, Optional<Map<String, Set<String>>> allowedTestingProductMap);
}
