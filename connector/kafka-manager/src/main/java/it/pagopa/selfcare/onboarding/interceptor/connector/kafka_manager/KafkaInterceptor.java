package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.PendingOnboardingNotification;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class KafkaInterceptor {

    private final Optional<Map<String, Set<String>>> institutionProductsAllowedMap;
    private final OnboardingValidationStrategy onboardingValidator;
    private final InternalApiConnector internalApiConnector;
    private final PendingOnboardingConnector pendingOnboardingConnector;
    static final Function<Institution, AutoApprovalOnboardingRequest> ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST = institution -> {
        AutoApprovalOnboardingRequest request = new AutoApprovalOnboardingRequest();
        List<GeographicTaxonomy> geoTaxonomies = new ArrayList<>();
        if (institution.getGeographicTaxonomies() != null && !institution.getGeographicTaxonomies().isEmpty()) {
            geoTaxonomies = institution.getGeographicTaxonomies();
        }
        request.setInstitutionType(institution.getInstitutionType());
        request.setOrigin(institution.getOrigin());
        request.setGeographicTaxonomies(geoTaxonomies);
        BillingData billings = new BillingData();
        billings.setBusinessName(institution.getDescription());
        billings.setRegisteredOffice(institution.getAddress());
        billings.setDigitalAddress(institution.getDigitalAddress());
        billings.setZipCode(institution.getZipCode());
        billings.setTaxCode(institution.getTaxCode());
        request.setBillingData(billings);
        request.setPspData(institution.getPaymentServiceProvider());
        request.getPspData().setDpoData(institution.getDataProtectionOfficer());
        request.setAssistanceContacts(institution.getAssistanceContacts());
        request.setCompanyInformations(institution.getCompanyInformations());
        return request;
    };

    @Autowired
    public KafkaInterceptor(@Value("#{${onboarding-interceptor.products-allowed-list}}") Map<String, Set<String>> institutionProductsAllowedMap,
                            OnboardingValidationStrategy onboardingValidator,
                            InternalApiConnector internalApiConnector,
                            PendingOnboardingConnector pendingOnboardingConnector) {
        this.onboardingValidator = onboardingValidator;
        log.info("Initializing {}...", KafkaInterceptor.class.getSimpleName());
        log.debug("institutionProductsAllowedMap = {}", institutionProductsAllowedMap);
        this.institutionProductsAllowedMap = Optional.ofNullable(institutionProductsAllowedMap);
        this.pendingOnboardingConnector = pendingOnboardingConnector;
        this.internalApiConnector = internalApiConnector;
    }

    @KafkaListener(topics = "${kafka-manager.onboarding-interceptor.topic}")
    public void intercept(InstitutionOnboardedNotification message) {
        log.trace("KafkaInterceptor intercept start");
        log.debug("KafkaInterceptor Incoming message: {}", message);
        final Institution institution = internalApiConnector.getInstitutionById(message.getInternalIstitutionID());
        final List<User> users = internalApiConnector.getInstitutionProductUsers(message.getInternalIstitutionID(), message.getProduct());
        final AutoApprovalOnboardingRequest request = ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST.apply(institution);
        request.setUsers(users);
        request.getBillingData().setRecipientCode(message.getBilling().getRecipientCode());
        request.getBillingData().setVatNumber(message.getBilling().getVatNumber());
        request.getBillingData().setPublicServices(message.getBilling().isPublicService());
        request.setPricingPlan(message.getPricingPlan());

        try {
            if (onboardingValidator.validate(message, institutionProductsAllowedMap)) {
                for (String productId : institutionProductsAllowedMap.get().get(message.getProduct())) {
                    internalApiConnector.autoApprovalOnboarding(message.getInstitution().getTaxCode(), productId, request);
                    log.debug("KafkaInterceptor onboarded request = {}", request);
                }
            }
        } catch (TestingProductUnavailableException | OnboardingFailedException e) {
            PendingOnboardingNotificationOperations pendingOnboarding = new PendingOnboardingNotification();
            pendingOnboarding.setId(message.getId());
            pendingOnboarding.setNotification(message);
            pendingOnboarding.setRequest(request);
            pendingOnboarding.setOnboardingFailure(e.getClass().getSimpleName());
            PendingOnboardingNotificationOperations saved = pendingOnboardingConnector.insert(pendingOnboarding);
            log.debug("Persisted failed onboarding request = {}", saved);
        } catch (InstitutionAlreadyOnboardedException e) {
            log.warn("[Already onboarded to Testing product] This institution {} has already onboarded the testing product of {}", message.getInternalIstitutionID(), message.getProduct());
        }
        log.trace("KafkaInterceptor intercept end");
    }


}
