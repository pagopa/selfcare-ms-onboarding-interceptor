package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.exception;

public class OnboardingFailedException extends RuntimeException {

    public OnboardingFailedException(String message) {
        super(message);
    }
}
