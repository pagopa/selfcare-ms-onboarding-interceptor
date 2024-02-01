package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.mapper;

import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {
    @Mappings(
            {
                    @Mapping(target = "companyInformations.rea", source = "rea"),
                    @Mapping(target = "companyInformations.shareCapital", source = "shareCapital"),
                    @Mapping(target = "companyInformations.businessRegisterPlace", source = "businessRegisterPlace"),
                    @Mapping(target = "assistanceContacts.supportEmail", source = "supportEmail"),
                    @Mapping(target = "assistanceContacts.supportPhone", source = "supportPhone"),
                    @Mapping(target = "subUnitType", source = "subunitType"),
                    @Mapping(target = "subUnitCode", source = "subunitCode"),
            }
    )
    Institution toInstitution(InstitutionResponse model);
}
