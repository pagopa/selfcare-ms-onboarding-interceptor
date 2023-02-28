package it.pagopa.selfcare.onboarding.interceptor.model.institution;

import lombok.Data;

import java.util.List;

@Data
public class AutoApprovalOnboardingRequest {

    private List<User> users;
    private BillingData billingData;
    private InstitutionType institutionType;
    private String origin;
    private String pricingPlan;
    private PspData pspData;
    private List<GeographicTaxonomy> geographicTaxonomies;
    private CompanyInformations companyInformations;
    private AssistanceContacts assistanceContacts;

}
