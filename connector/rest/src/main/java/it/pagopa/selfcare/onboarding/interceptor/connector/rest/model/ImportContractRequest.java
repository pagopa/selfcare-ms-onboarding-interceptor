package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model;

import lombok.Data;

@Data
public class ImportContractRequest {

    private String fileName;
    private String filePath;
    private String contractType;

}
