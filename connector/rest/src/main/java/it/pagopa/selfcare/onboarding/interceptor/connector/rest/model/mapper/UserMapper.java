package it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.mapper;

import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.UserResponse;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "taxCode", source = "fiscalCode")
    User toUser(UserResponse response);
}
