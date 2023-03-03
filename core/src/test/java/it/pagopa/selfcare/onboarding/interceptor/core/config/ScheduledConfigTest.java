package it.pagopa.selfcare.onboarding.interceptor.core.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(ScheduledConfig.class)
class ScheduledConfigTest {

}