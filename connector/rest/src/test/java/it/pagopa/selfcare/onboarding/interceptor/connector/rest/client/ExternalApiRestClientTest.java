package it.pagopa.selfcare.onboarding.interceptor.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.FeignException;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.config.ExternalApiRestClientTestConfig;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestPropertySource(
        locations = "classpath:config/external-api-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-onboarding-interceptor-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = ExternalApiRestClientTest.RandomPortInitializer.class,
        classes = {ExternalApiRestClientTestConfig.class, HttpClientConfiguration.class})
class ExternalApiRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/external-api"))
            .build();

    @Autowired
    private ExternalApiRestClient restClient;


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("EXTERNAL_API_SERVICE_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Test
    void autoApprovalOnboarding() {
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
    void autoApprovalOnboarding_badRequest() {
        // given
        String institutionId = "institutionId2";
        String productId = "productId2";
        AutoApprovalOnboardingRequest request = mockInstance(new AutoApprovalOnboardingRequest());
        request.setUsers(List.of(mockInstance(new User())));
        request.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        Executable executable = () -> restClient.autoApprovalOnboarding(institutionId, productId, request);
        // then
        assertThrows(FeignException.BadRequest.class, executable);
    }

}