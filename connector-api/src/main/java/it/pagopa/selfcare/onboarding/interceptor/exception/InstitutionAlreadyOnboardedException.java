package it.pagopa.selfcare.onboarding.interceptor.exception;

public class InstitutionAlreadyOnboardedException extends RuntimeException {
    public InstitutionAlreadyOnboardedException(String message) {
        super(message);
    }

    public InstitutionAlreadyOnboardedException() {

    }
}
