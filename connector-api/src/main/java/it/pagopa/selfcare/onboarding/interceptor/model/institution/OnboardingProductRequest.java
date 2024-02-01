package it.pagopa.selfcare.onboarding.interceptor.model.institution;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

import java.util.List;
@Data
public class OnboardingProductRequest {
    private List<User> users;

    private BillingData billingData;

    private InstitutionLocationData institutionLocationData;

    private InstitutionType institutionType;

    private String origin;

    private String pricingPlan;

    private PspData pspData;

    private List<GeographicTaxonomy> geographicTaxonomies;

    private CompanyInformations companyInformations;

    private AssistanceContacts assistanceContacts;

    private String productId;

    private String taxCode;

    private String subunitCode;

    private String subunitType;
}
