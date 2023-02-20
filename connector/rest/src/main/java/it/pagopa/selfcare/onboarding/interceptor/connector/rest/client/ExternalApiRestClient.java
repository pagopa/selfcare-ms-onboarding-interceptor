package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.UserResponse;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "${rest-client.external-api.serviceCode}", url = "${rest-client.external-api.base-url}")
public interface ExternalApiRestClient {

    @PostMapping(value = "${rest-client.external-api.autoApprovalOnboarding.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void autoApprovalOnboarding(@PathVariable("externalInstitutionId") String externalInstitutionId,
                                @PathVariable("productId") String productId,
                                @RequestBody AutoApprovalOnboardingRequest request);

    @GetMapping(value = "${rest-client.external-api.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionResponse getInstitutionById(@PathVariable("id") String institutionId);

    @GetMapping(value = "${rest-client.external-api.getInstitutionProductUsers.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<UserResponse> getInstitutionProductUsers(@PathVariable("institutionId") String institutionId,
                                                  @PathVariable("productId") String productId);

}
