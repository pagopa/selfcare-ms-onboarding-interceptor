package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.config.InternalApiRestClientTestConfig;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestPropertySource(
        locations = "classpath:config/internal-api-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.onboarding-interceptor.connector.rest=DEBUG",
                "spring.application.name=selc-onboarding-interceptor-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = InternalApiRestClientTest.RandomPortInitializer.class,
        classes = {InternalApiRestClientTestConfig.class, HttpClientConfiguration.class})
class InternalApiRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/internal-api"))
            .build();

    @Autowired
    private InternalApiRestClient restClient;


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("INTERNAL_API_SERVICE_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Test
    void autoApprovalOnboarding_fullyValued() {
        // given
        String institutionId = "institutionId1";
        String productId = "productId1";
        AutoApprovalOnboardingRequest request = mockInstance(new AutoApprovalOnboardingRequest());
        request.setUsers(List.of(mockInstance(new User())));
        request.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        Executable executable = () -> restClient.autoApprovalOnboarding(institutionId, productId, request);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void autoApprovalOnboarding_fullyNull() {
        // given
        String institutionId = "institutionId2";
        String productId = "productId2";
        AutoApprovalOnboardingRequest request = mockInstance(new AutoApprovalOnboardingRequest());
        request.setUsers(List.of(mockInstance(new User())));
        request.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        Executable executable = () -> restClient.autoApprovalOnboarding(institutionId, productId, request);
        // then
        assertDoesNotThrow(executable);
    }

}