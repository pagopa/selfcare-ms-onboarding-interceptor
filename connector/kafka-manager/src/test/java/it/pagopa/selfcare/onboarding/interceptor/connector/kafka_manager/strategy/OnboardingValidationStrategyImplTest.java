package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.strategy;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import it.pagopa.selfcare.onboarding.interceptor.model.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingValidationStrategyImplTest {

    @InjectMocks
    OnboardingValidationStrategyImpl validationStrategy;
    private Optional<Map<String, Set<String>>> allowedProductsMap = Optional.of(Map.of("prod-interop", Set.of("prod-interop-coll")));
    @Mock
    InternalApiConnector internalApiConnector;

    @Test
    void validate_false() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        message.setProduct("coll");
        //when
        boolean validation = validationStrategy.validate(message, allowedProductsMap);
        //then
        assertFalse(validation);
        verifyNoInteractions(internalApiConnector);
    }

    @Test
    void validate_true() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        message.setProduct("prod-interop");
        Product product = returnProductMock();
        product.setId("prod-interop-coll");
        product.setStatus(ProductStatus.TESTING);
        when(internalApiConnector.getProduct(any()))
                .thenReturn(product);
        //when
        boolean validation = validationStrategy.validate(message, allowedProductsMap);
        //then
        assertTrue(validation);
        verify(internalApiConnector, times(1))
                .getProduct(product.getId());
        verifyNoMoreInteractions(internalApiConnector);
    }

    @Test
    void validate_OnboardingFailed() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        //when
        Executable executable = () -> validationStrategy.validate(message, allowedProductsMap);
        //then
        OnboardingFailedException exception = assertThrows(OnboardingFailedException.class, executable);
        assertEquals(String.format("[Test - Onboarding - Error]No Testing products available for %s, onboarding-request = %s", message.getProduct(), message), exception.getMessage());
        verifyNoInteractions(internalApiConnector);
    }

    @Test
    void validate_ProductNoLongerInTest() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        message.setProduct("prod-interop");
        Product product = returnProductMock();
        product.setId("prod-interop-coll");
        product.setStatus(ProductStatus.PHASE_OUT);
        when(internalApiConnector.getProduct(any()))
                .thenReturn(product);
        //when
        Executable executable = () -> validationStrategy.validate(message, allowedProductsMap);
        //then
        TestingProductUnavailableException exception = assertThrows(TestingProductUnavailableException.class, executable);
        assertEquals(String.format("[ProductStatus - Error] Product %s no longer available", product.getId()), exception.getMessage());
        verify(internalApiConnector, times(1))
                .getProduct(product.getId());
        verifyNoMoreInteractions(internalApiConnector);
    }

    @Test
    void validate_productMapNotPresent() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        //when
        Executable executable = () -> validationStrategy.validate(message, Optional.empty());
        //then
        OnboardingFailedException exception = assertThrows(OnboardingFailedException.class, executable);
        assertEquals(String.format("[Test - Onboarding - Error]No Testing products available for %s, onboarding-request = %s", message.getProduct(), message), exception.getMessage());
        verifyNoInteractions(internalApiConnector);
    }

    @Test
    void validate_ProductNotFound() {
        //given
        InstitutionOnboardedNotification message = mockInstance(new InstitutionOnboardedNotification());
        message.setProduct("prod-interop");
        Product product = returnProductMock();
        product.setId("prod-interop-coll");
        product.setStatus(ProductStatus.PHASE_OUT);
        doThrow(ResourceNotFoundException.class)
                .when(internalApiConnector)
                .getProduct(any());
        //when
        Executable executable = () -> validationStrategy.validate(message, allowedProductsMap);
        //then
        TestingProductUnavailableException exception = assertThrows(TestingProductUnavailableException.class, executable);
        assertEquals(String.format("[ProductStatus - Error] Product %s no longer available, error: NotFound", product.getId()), exception.getMessage());
        verify(internalApiConnector, times(1))
                .getProduct(product.getId());
        verifyNoMoreInteractions(internalApiConnector);
    }

    private Product returnProductMock() {
        Product product = mockInstance(new Product());
        return product;
    }
}