package it.pagopa.selfcare.onboarding.interceptor.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.client.InternalApiRestClient;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.UserResponse;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static it.pagopa.selfcare.onboarding.interceptor.connector.rest.InternalApiConnectorImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InternalApiConnectorImplTest {

    @InjectMocks
    private InternalApiConnectorImpl internalApiConnectorMock;

    @Mock
    private InternalApiRestClient restClientMock;

    private final ObjectMapper mapper;

    public InternalApiConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

    @Test
    void autoApprovalOnboarding() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        AutoApprovalOnboardingRequest autoApprovalOnboardingRequestMock = mockInstance(new AutoApprovalOnboardingRequest());
        autoApprovalOnboardingRequestMock.setUsers(List.of(mockInstance(new User())));
        autoApprovalOnboardingRequestMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        internalApiConnectorMock.autoApprovalOnboarding(institutionId, productId, autoApprovalOnboardingRequestMock);
        // then
        verify(restClientMock, times(1))
                .autoApprovalOnboarding(institutionId, productId, autoApprovalOnboardingRequestMock);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void autoApprovalOnboarding_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        AutoApprovalOnboardingRequest autoApprovalOnboardingRequestMock = mockInstance(new AutoApprovalOnboardingRequest());
        autoApprovalOnboardingRequestMock.setUsers(List.of(mockInstance(new User())));
        autoApprovalOnboardingRequestMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        Executable executable = () -> internalApiConnectorMock.autoApprovalOnboarding(institutionId, productId, autoApprovalOnboardingRequestMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(EXTERNAL_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void autoApprovalOnboarding_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        AutoApprovalOnboardingRequest autoApprovalOnboardingRequestMock = mockInstance(new AutoApprovalOnboardingRequest());
        autoApprovalOnboardingRequestMock.setUsers(List.of(mockInstance(new User())));
        autoApprovalOnboardingRequestMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        Executable executable = () -> internalApiConnectorMock.autoApprovalOnboarding(institutionId, productId, autoApprovalOnboardingRequestMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(PRODUCT_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionById() {
        // given
        InstitutionResponse institutionResponseMock = mockInstance(new InstitutionResponse());
        institutionResponseMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionResponseMock.setAttributes(List.of(mockInstance(new Attribute())));
        when(restClientMock.getInstitutionById(institutionResponseMock.getId()))
                .thenReturn(institutionResponseMock);
        // when
        Institution result = internalApiConnectorMock.getInstitutionById(institutionResponseMock.getId());
        // then
        assertNotNull(result);
        reflectionEqualsByName(institutionResponseMock, result);
        reflectionEqualsByName(institutionResponseMock, result.getCompanyInformations());
        reflectionEqualsByName(institutionResponseMock, result.getAssistanceContacts());
        verify(restClientMock, times(1))
                .getInstitutionById(institutionResponseMock.getId());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitution_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> internalApiConnectorMock.getInstitutionById(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionProductUsers() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        List<UserResponse> userResponseMock = List.of(mockInstance(new UserResponse()));
        userResponseMock.get(0).setRoles(List.of("role"));
        when(restClientMock.getInstitutionProductUsers(any(), any()))
                .thenReturn(userResponseMock);
        // when
        List<User> result = internalApiConnectorMock.getInstitutionProductUsers(institutionId, productId);
        // then
        assertNotNull(result);
        reflectionEqualsByName(userResponseMock.get(0), result.get(0));
        assertEquals(userResponseMock.get(0).getFiscalCode(), result.get(0).getTaxCode());
        verify(restClientMock, times(1))
                .getInstitutionProductUsers(institutionId, productId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionProductUsers_emptyList() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        List<UserResponse> userResponseMock = Collections.emptyList();
        when(restClientMock.getInstitutionProductUsers(any(), any()))
                .thenReturn(userResponseMock);
        // when
        List<User> result = internalApiConnectorMock.getInstitutionProductUsers(institutionId, productId);
        // then
        assertTrue(result.isEmpty());
        verify(restClientMock, times(1))
                .getInstitutionProductUsers(institutionId, productId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionProductUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        // when
        Executable executable = () -> internalApiConnectorMock.getInstitutionProductUsers(institutionId, productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionProductUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        // when
        Executable executable = () -> internalApiConnectorMock.getInstitutionProductUsers(institutionId, productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(PRODUCT_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getProduct() {
        // given
        Product productMock = mockInstance(new Product());
        when(restClientMock.getProduct(any()))
                .thenReturn(productMock);
        // when
        Product result = internalApiConnectorMock.getProduct(productMock.getId());
        // then
        assertNotNull(result);
        verify(restClientMock, times(1))
                .getProduct(productMock.getId());
        verifyNoMoreInteractions(restClientMock);
    }


}
