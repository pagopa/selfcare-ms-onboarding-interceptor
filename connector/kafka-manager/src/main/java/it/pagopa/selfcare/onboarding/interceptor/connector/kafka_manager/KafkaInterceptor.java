package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.PendingOnboardingNotification;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

import static it.pagopa.selfcare.onboarding.interceptor.model.product.ProductStatus.TESTING;

@Slf4j
@Service
public class KafkaInterceptor {

    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required";
    private final Optional<Map<String, Set<String>>> institutionProductsAllowedMap;

    private static final String TESTING_PRODUCT_SUFFIX = "coll";
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
        request.setAssistanceContacts(institution.getAssistanceContacts());
        request.setCompanyInformations(institution.getCompanyInformations());
        return request;
    };

    @Autowired
    public KafkaInterceptor(@Value("#{${onboarding-interceptor.products-allowed-list}}") Map<String, Set<String>> institutionProductsAllowedMap,
                            InternalApiConnector internalApiConnector,
                            PendingOnboardingConnector pendingOnboardingConnector) {
        log.trace("Initializing {}", KafkaInterceptor.class.getSimpleName());
        log.debug("institutionProductsAllowedMap = {}", institutionProductsAllowedMap);
        this.institutionProductsAllowedMap = Optional.ofNullable(institutionProductsAllowedMap);
        this.pendingOnboardingConnector = pendingOnboardingConnector;
        this.internalApiConnector = internalApiConnector;
    }

    @KafkaListener(topics = "${kafka-manager.onboarding-interceptor.topic}")
    public void intercept(final InstitutionOnboardedNotification message) {
        log.trace("KafkaInterceptor intercept start");
        log.debug("KafkaInterceptor Incoming message: {}", message);
        Assert.hasText(message.getInternalIstitutionID(), INSTITUTION_ID_IS_REQUIRED);
        final Institution institution = internalApiConnector.getInstitutionById(message.getInternalIstitutionID());
        final List<User> users = internalApiConnector.getInstitutionProductUsers(message.getInternalIstitutionID(), message.getProduct());
        final AutoApprovalOnboardingRequest request = ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST.apply(institution);
        request.setUsers(users);
        request.getBillingData().setRecipientCode(message.getBilling().getRecipientCode());
        request.getBillingData().setVatNumber(message.getBilling().getVatNumber());
        request.getBillingData().setPublicServices(message.getBilling().isPublicService());
        request.setPricingPlan(message.getPricingPlan());
        try {
            if (validateTestingProduct(message)) {
                for (String productId : institutionProductsAllowedMap.get().get(message.getProduct())) {
                    internalApiConnector.autoApprovalOnboarding(message.getInstitution().getTaxCode(), productId, request);
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
        }
        log.debug("KafkaInterceptor Request to onboard = {}", request);
        log.trace("KafkaInterceptor intercept end");
    }

    private boolean validateTestingProduct(InstitutionOnboardedNotification message) {
        if (message.getProduct().contains(TESTING_PRODUCT_SUFFIX)) {
            return false;
        }
        if (institutionProductsAllowedMap.isPresent() && institutionProductsAllowedMap.get().containsKey(message.getProduct())) {
            for (String productId : institutionProductsAllowedMap.get().get(message.getProduct())) {
                try {
                    Product product = internalApiConnector.getProduct(productId);
                    if (!TESTING.equals(product.getStatus())){
                        log.error("[ProductStatus - Error]Product {} no longer in TESTING, onboarding not enabled", productId);
                        throw new TestingProductUnavailableException(String.format("[ProductStatus - Error] Product %s no longer available", productId));
                    }
                }catch (RuntimeException e) {
                    log.error("[ProductStatus - Error]Product {} no longer in TESTING, onboarding not enabled", productId);
                    throw new TestingProductUnavailableException(String.format("[ProductStatus - Error] Product %s no longer available, error: %s", productId, e.getMessage()));
                }
            }
        } else {
            log.error("[Onboarding - Error]No Testing products available for {}, onboarding-request = {}", message.getProduct(), message);
            throw new OnboardingFailedException(String.format("[Test - Onboarding - Error]No Testing products available for %s, onboarding-request = %s", message.getProduct(), message));
        }
        return true;
    }

}
