package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

@Data
public class UserRequest {

    private String name;
    private String surname;
    private String taxCode;
    private PartyRole role;
    private String email;
    private String productRole;

}
