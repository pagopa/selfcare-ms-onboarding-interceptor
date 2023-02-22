package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.PendingOnboardingNotificationOperations;

import java.util.List;
import java.util.Optional;

public interface DaoConnector {
    PendingOnboardingNotificationOperations insert(PendingOnboardingNotificationOperations entity);

    PendingOnboardingNotificationOperations save(PendingOnboardingNotificationOperations entity);

    boolean existsById(String id);

    void deleteById(String id);

    Optional<PendingOnboardingNotificationOperations> findById(String id);

    List<PendingOnboardingNotificationOperations> findAll();
}
