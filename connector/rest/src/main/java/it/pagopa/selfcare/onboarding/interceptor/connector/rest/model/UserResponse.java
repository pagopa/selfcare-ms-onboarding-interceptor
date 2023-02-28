package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    private String name;
    private String surname;
    private String fiscalCode;
    private PartyRole role;
    private String email;
    private List<String> roles;

}
