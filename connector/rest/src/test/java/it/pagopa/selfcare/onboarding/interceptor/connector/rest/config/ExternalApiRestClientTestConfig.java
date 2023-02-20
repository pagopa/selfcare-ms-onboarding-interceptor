package it.pagopa.selfcare.onboarding.interceptor.connector.rest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(ExternalApiRestClientConfig.class)
public class ExternalApiRestClientTestConfig {
}
