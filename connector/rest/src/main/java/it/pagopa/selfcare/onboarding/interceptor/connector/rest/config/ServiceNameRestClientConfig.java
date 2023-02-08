package it.pagopa.selfcare.onboarding.interceptor.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.client.PartyManagementRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = PartyManagementRestClient.class)
@PropertySource("classpath:config/party-management-rest-client.properties")
class PartyManagementRestClientConfig {
}
