package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.strategy;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.selfcare.onboarding.interceptor.model.product.ProductStatus.TESTING;

@Service
@Slf4j
public class OnboardingValidationStrategyImpl implements OnboardingValidationStrategy {
    private static final String TESTING_PRODUCT_SUFFIX = "coll";
    private final InternalApiConnector internalApiConnector;

    public OnboardingValidationStrategyImpl(InternalApiConnector internalApiConnector) {
        log.info("Initializing {}...", OnboardingValidationStrategyImpl.class.getSimpleName());
        this.internalApiConnector = internalApiConnector;
    }

    @Override
    public boolean validate(InstitutionOnboardedNotification message, Optional<Map<String, Set<String>>> allowedTestingProductMap) {
        log.trace("validate start");
        if (message.getProduct().contains(TESTING_PRODUCT_SUFFIX)) {
            log.debug("validate result = {}", false);
            return false;
        }
        if (allowedTestingProductMap.isPresent() && allowedTestingProductMap.get().containsKey(message.getProduct())) {
            for (String productId : allowedTestingProductMap.get().get(message.getProduct())) {
                try {
                    Product product = internalApiConnector.getProduct(productId);
                    if (!TESTING.equals(product.getStatus())) {
                        log.error("[ProductStatus - Error]Product {} no longer in TESTING, onboarding not enabled. Product status is: {}", productId, product.getStatus());
                        throw new TestingProductUnavailableException(String.format("[ProductStatus - Error] Product %s no longer available", productId));
                    }
                } catch (ResourceNotFoundException e) {
                    log.error("[ProductStatus - Error]Product {} no longer in TESTING, onboarding not enabled. Reason: {}}", productId, e.getCause());
                    throw new TestingProductUnavailableException(String.format("[ProductStatus - Error] Product %s no longer available, error: NotFound", productId));
                }
            }
        } else {
            log.error("[Onboarding - Error]No Testing products available for {}, onboarding-request = {}", message.getProduct(), message);
            throw new OnboardingFailedException(String.format("[Test - Onboarding - Error]No Testing products available for %s, onboarding-request = %s", message.getProduct(), message));
        }
        log.debug("validate result = {}", true);
        return true;
    }
}
