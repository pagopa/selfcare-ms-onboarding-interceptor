package it.pagopa.selfcare.onboarding.interceptor.model.kafka;

import lombok.Data;

@Data
public class NotifiedPsp {
    private String abiCode;
    private String businessRegisterNumber;
    private String legalRegisterName;
    private String legalRegisterNumber;
    private boolean vatNumberGroup;
}
