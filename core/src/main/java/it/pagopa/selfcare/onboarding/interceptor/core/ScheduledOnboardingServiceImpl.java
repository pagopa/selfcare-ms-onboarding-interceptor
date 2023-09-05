package it.pagopa.selfcare.onboarding.interceptor.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Qualifier("core")
public class ScheduledOnboardingServiceImpl implements ScheduledOnboardingService {

    private final PendingOnboardingConnector pendingOnboardingConnector;
    private final InternalApiConnector internalApiConnector;
    private final OnboardingValidationStrategy onboardingValidator;
    private final Optional<Map<String, Set<String>>> institutionProductsAllowedMap;

    @Autowired
    public ScheduledOnboardingServiceImpl(PendingOnboardingConnector pendingOnboardingConnector,
                                          InternalApiConnector internalApiConnector,
                                          OnboardingValidationStrategy onboardingValidator,
                                          @Value("#{${onboarding-interceptor.products-allowed-list}}")
                                              Map<String, Set<String>> institutionProductsAllowedMap) {
        log.info("Initializing {}...", ScheduledOnboardingServiceImpl.class.getSimpleName());
        this.onboardingValidator = onboardingValidator;
        this.institutionProductsAllowedMap = Optional.ofNullable(institutionProductsAllowedMap);
        this.pendingOnboardingConnector = pendingOnboardingConnector;
        this.internalApiConnector = internalApiConnector;
    }


    @Override
    @Scheduled(fixedDelayString = "${scheduler.fixed-delay.delay}")
    public void retry() {
        log.trace("ScheduledOnboardingServiceImpl retry start");
        PendingOnboardingNotificationOperations oldest = pendingOnboardingConnector.findOldest();
        try {
            if (oldest != null && onboardingValidator.validate(oldest.getNotification(), institutionProductsAllowedMap)) {
                for (String productId : institutionProductsAllowedMap.get().get(oldest.getNotification().getProduct())) {
                    internalApiConnector.autoApprovalOnboarding(oldest.getNotification().getInstitution().getTaxCode(),
                            productId,
                            oldest.getRequest());
                    pendingOnboardingConnector.deleteById(oldest.getId());
                    log.debug(LogUtils.CONFIDENTIAL_MARKER, "KafkaInterceptor onboarded request = {}", oldest);
                }
            }
        } catch (TestingProductUnavailableException | OnboardingFailedException e) {
            oldest.setOnboardingFailure(e.getClass().getSimpleName());
            pendingOnboardingConnector.insert(oldest);
        } catch (InstitutionAlreadyOnboardedException e) {
            pendingOnboardingConnector.deleteById(oldest.getId());
            log.warn("[Already onboarded to Testing product] This institution {} has already onboarded the testing product of {}",
                    oldest.getNotification().getInstitution().getDescription(),
                    oldest.getNotification().getProduct());
        }
        log.trace("ScheduledOnboardingServiceImpl retry end");
    }
}
