package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.UserResponse;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "${rest-client.internal-api.serviceCode}", url = "${rest-client.internal-api.base-url}")
public interface InternalApiRestClient {

    @PostMapping(value = "${rest-client.internal-api.autoApprovalOnboarding.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void autoApprovalOnboarding(@PathVariable("externalInstitutionId") String externalInstitutionId,
                                @PathVariable("productId") String productId,
                                @RequestBody AutoApprovalOnboardingRequest request);

    @GetMapping(value = "${rest-client.internal-api.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionResponse getInstitutionById(@PathVariable("id") String institutionId);

    @GetMapping(value = "${rest-client.internal-api.getInstitutionProductUsers.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<UserResponse> getInstitutionProductUsers(@PathVariable("institutionId") String institutionId,
                                                  @PathVariable("productId") String productId);

    @GetMapping(value = "${rest-client.internal-api.getProduct.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Product getProduct(@PathVariable("productId") String productId);

    @PostMapping(value  = "${rest-client.internal-api.onboarding.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboarding(@RequestBody OnboardingProductRequest request);

}
