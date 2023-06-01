package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.ExceptionOperations;

public interface ExceptionDaoConnector {

    ExceptionOperations insert(String payload, Exception exception);
}
