package it.pagopa.selfcare.onboarding.interceptor.api;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.InstitutionInfo;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;

import java.util.List;

public interface ExternalApiConnector {

    Institution getInstitution(String institutionId);

    List<InstitutionInfo> getInstitutions(String productId);

    List<Product> getInstitutionUserProducts(String institutionId);

}
