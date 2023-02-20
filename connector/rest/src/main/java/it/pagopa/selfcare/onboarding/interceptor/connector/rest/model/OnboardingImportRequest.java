package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model;

import lombok.Data;

import java.util.List;

@Data
public class OnboardingImportRequest {

    private List<UserRequest> users;
    private ImportContractRequest importContract;
}
