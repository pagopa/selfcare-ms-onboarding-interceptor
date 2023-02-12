package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config;

import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;

public interface KafkaConnector {
    void intercept(InstitutionOnboardedNotification in);
}
