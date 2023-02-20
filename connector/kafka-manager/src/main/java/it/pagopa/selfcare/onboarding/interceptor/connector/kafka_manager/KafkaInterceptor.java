package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.api.ExternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class KafkaInterceptor {

    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required";
    private final Optional<Map<String, String>> institutionProductsAllowedMap;

    private final ExternalApiConnector externalApiConnector;
    private Function<Institution, AutoApprovalOnboardingRequest> ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST = institution -> {
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
    public KafkaInterceptor(@Value("${onboarding-interceptor.products-allowed-list}") Map<String, String> institutionProductsAllowedMap,
                            ExternalApiConnector externalApiConnector) {
        log.trace("Initializing {}", KafkaInterceptor.class.getSimpleName());
        this.institutionProductsAllowedMap = Optional.ofNullable(institutionProductsAllowedMap);
        this.externalApiConnector = externalApiConnector;
    }

    @KafkaListener(topics = "${kafka-manager.onboarding-interceptor.topic}")
    public void intercept(InstitutionOnboardedNotification message) {
        log.debug("Incoming message: {}", message);
        Assert.hasText(message.getInternalIstitutionID(), INSTITUTION_ID_IS_REQUIRED);
        Institution institution = externalApiConnector.getInstitutionById(message.getInternalIstitutionID());
        List<User> users = externalApiConnector.getInstitutionProductUsers(message.getInternalIstitutionID(), message.getProduct());
        AutoApprovalOnboardingRequest request = ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST.apply(institution);
        request.setUsers(users);
        if (institutionProductsAllowedMap.isPresent() && institutionProductsAllowedMap.get().containsKey(message.getProduct())) {
            externalApiConnector.autoApprovalOnboarding(message.getInstitution().getTaxCode(), institutionProductsAllowedMap.get().get(message.getProduct()), request);
        } else {
            log.error("No Testing products available for {}", message.getProduct());
            throw new OnboardingFailedException(String.format("No Testing products fond for product %s", message.getProduct()));
        }
        log.debug("Request to onboard = {}", request);
    }

}
