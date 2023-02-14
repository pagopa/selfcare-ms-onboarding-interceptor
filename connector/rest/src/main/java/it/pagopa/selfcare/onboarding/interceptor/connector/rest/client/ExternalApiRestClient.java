package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import it.pagopa.selfcare.onboarding.interceptor.api.ExternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.OnboardingImportRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.InstitutionInfo;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "${rest-client.external-api.serviceCode}", url = "${rest-client.external-api.base-url}")
public interface ExternalApiRestClient extends ExternalApiConnector {

    @GetMapping(value = "${rest-client.external-api.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitution(@PathVariable(value = "institutionId") String id);

    @GetMapping(value = "${rest-client.external-api.getInstitutions.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<InstitutionInfo> getInstitutions(@RequestParam(value = "productId") String productId);

    @GetMapping(value = "${rest-client.external-api.getInstitutionUserProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<Product> getInstitutionUserProducts(@PathVariable(value = "institutionId") String institutionId);

    @PostMapping(value = "${rest-client.external-api.onboardingOldContracts.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void oldContractOnboarding(@RequestBody OnboardingImportRequest request);


//    @PostMapping(value = "${rest-client.external-api.autoApprovalOnboarding.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    void autoApprovalOnboarding(@RequestBody)


}
