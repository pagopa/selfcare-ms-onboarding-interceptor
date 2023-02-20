package it.pagopa.selfcare.onboarding.interceptor.model.institution;

import lombok.Data;

@Data
public class Billing {
    private String vatNumber;
    private String recipientCode;
    private Boolean publicServices;
}
