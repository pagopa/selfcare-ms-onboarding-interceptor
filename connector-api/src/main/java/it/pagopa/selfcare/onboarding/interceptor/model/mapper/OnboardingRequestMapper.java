package it.pagopa.selfcare.onboarding.interceptor.model.mapper;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OnboardingRequestMapper {

    @Mappings({
            @Mapping(target = "institutionLocationData.city", source= "notification.institution.city"),
            @Mapping(target = "institutionLocationData.county", source= "notification.institution.county"),
            @Mapping(target = "institutionLocationData.country", source= "notification.institution.country"),
            @Mapping(target = "billingData.businessName", source = "notification.institution.description"),
            @Mapping(target = "billingData.registeredOffice", source = "notification.institution.address"),
            @Mapping(target = "billingData.digitalAddress", source = "notification.institution.digitalAddress"),
            @Mapping(target = "billingData.zipCode", source = "notification.institution.zipCode"),
            @Mapping(target = "billingData.taxCode", source = "notification.institution.taxCode"),
            @Mapping(target = "billingData.vatNumber", source = "notification.billing.vatNumber"),
            @Mapping(target = "billingData.recipientCode", source = "notification.billing.vatNumber"),
            @Mapping(target = "billingData.publicServices", source = "notification.billing.publicServices"),
            @Mapping(target = "institutionType", source = "notification.institution.institutionType"),
            @Mapping(target = "taxCode", source = "notification.institution.taxCode"),
            @Mapping(target = "subunitCode", source = "notification.institution.subUnitCode"),
            @Mapping(target = "subunitType", source = "notification.institution.subUnitType"),
            @Mapping(target = "origin", source = "notification.institution.origin"),
            @Mapping(target = "productId", source = "notification.product"),
            @Mapping(target = "pspData", source = "notification.institution.paymentServiceProvider"),
    })
    OnboardingProductRequest toOnboardingRequest(InstitutionOnboardedNotification notification, List<User> users);
}
