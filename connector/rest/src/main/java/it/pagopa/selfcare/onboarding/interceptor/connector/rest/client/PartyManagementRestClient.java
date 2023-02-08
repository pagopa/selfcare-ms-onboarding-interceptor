package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.party-management.serviceCode}", url = "${rest-client.party-management.base-url}")
public interface PartyManagementRestClient {
}
