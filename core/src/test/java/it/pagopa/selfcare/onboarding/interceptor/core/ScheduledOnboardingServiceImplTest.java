package it.pagopa.selfcare.onboarding.interceptor.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.core.config.ScheduledConfig;
import it.pagopa.selfcare.onboarding.interceptor.core.model.DummyPendingOnboardingNotification;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Billing;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionToNotify;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        ScheduledOnboardingServiceImpl.class, ScheduledConfig.class
})
@TestPropertySource(properties = {
        "onboarding-interceptor.products-allowed-list={'prod-interop':{'prod-interop-coll'}}",
        "scheduler.fixed-delay.delay=2000",
        "scheduler.threads.max-number=1"
})
class ScheduledOnboardingServiceImplTest {

    @SpyBean
    private ScheduledOnboardingServiceImpl scheduler;
    @MockBean
    private PendingOnboardingConnector pendingOnboardingConnector;

    @MockBean
    private InternalApiConnector internalApiConnector;
    @MockBean
    private OnboardingValidationStrategy validationStrategy;

    private Optional<Map<String, Set<String>>> allowedProductsMap = Optional.of(Map.of("prod-interop", Set.of("prod-interop-coll")));

    @BeforeEach
    void setUp() throws InterruptedException {
        reset(scheduler, internalApiConnector, pendingOnboardingConnector, validationStrategy);
        Thread.sleep(1000);
    }

    @Test
    void retry_successful() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        doReturn(oldest)
                .when(pendingOnboardingConnector)
                .findOldest();
        doReturn(true)
                .when(validationStrategy)
                .validate(any(), any());

        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //then
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        verify(internalApiConnector, timeout(3000).times(1))
                .onboarding(oldest.getRequest());
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .deleteById(oldest.getId());
        verifyNoMoreInteractions(pendingOnboardingConnector, internalApiConnector);

    }

    @Test
    void retry_institutionAlreadyExists() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        doReturn(oldest)
                .when(pendingOnboardingConnector)
                .findOldest();
        doReturn(true)
                .when(validationStrategy)
                .validate(any(), any());
        doThrow(InstitutionAlreadyOnboardedException.class)
                .when(internalApiConnector)
                .autoApprovalOnboarding(any(), any(), any());

        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //the
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        verify(internalApiConnector, timeout(3000).times(1))
                .onboarding(oldest.getRequest());
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .deleteById(oldest.getId());
        verifyNoMoreInteractions(pendingOnboardingConnector, internalApiConnector);
    }

    @Test
    void retry_notOnboarded_oldestNull() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        doReturn(true)
                .when(validationStrategy)
                .validate(any(), any());
        doThrow(InstitutionAlreadyOnboardedException.class)
                .when(internalApiConnector)
                .autoApprovalOnboarding(any(), any(), any());

        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //the
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        verifyNoInteractions(internalApiConnector);
        verifyNoMoreInteractions(pendingOnboardingConnector);
    }

    @Test
    void retry_notOnboarded_validationFalse() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        doReturn(oldest)
                .when(pendingOnboardingConnector)
                .findOldest();
        doReturn(false)
                .when(validationStrategy)
                .validate(any(), any());
        doThrow(InstitutionAlreadyOnboardedException.class)
                .when(internalApiConnector)
                .autoApprovalOnboarding(any(), any(), any());

        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //the
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        verifyNoInteractions(internalApiConnector);
        verifyNoMoreInteractions(pendingOnboardingConnector);
    }

    @Test
    void retry_onboardingFailedException() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        oldest.setOnboardingFailure(TestingProductUnavailableException.class.getSimpleName());
        doReturn(oldest)
                .when(pendingOnboardingConnector)
                .findOldest();
        doThrow(OnboardingFailedException.class)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //the
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        ArgumentCaptor<PendingOnboardingNotificationOperations> pendingOnboardCaptor = ArgumentCaptor.forClass(PendingOnboardingNotificationOperations.class);
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .insert(pendingOnboardCaptor.capture());
        PendingOnboardingNotificationOperations captured = pendingOnboardCaptor.getValue();
        assertEquals(OnboardingFailedException.class.getSimpleName(), captured.getOnboardingFailure());
        verifyNoInteractions(internalApiConnector);
        verifyNoMoreInteractions(pendingOnboardingConnector);
    }

    @Test
    void retry_TestingProductFailedException() {
        //given
        PendingOnboardingNotificationOperations oldest = TestUtils.mockInstance(new DummyPendingOnboardingNotification());
        InstitutionOnboardedNotification notification = returnNotificationMock(1);
        notification.setProduct("prod-interop");
        notification.setInstitution(mockInstance(new InstitutionToNotify()));
        oldest.setNotification(notification);
        oldest.setRequest(returnRequestMock());
        oldest.setOnboardingFailure(OnboardingFailedException.class.getSimpleName());
        doReturn(oldest)
                .when(pendingOnboardingConnector)
                .findOldest();
        doThrow(TestingProductUnavailableException.class)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        await()
                .atMost(Duration.TWO_SECONDS)
                .untilAsserted(() -> verify(scheduler, times(1)).retry());
        //the
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .findOldest();
        ArgumentCaptor<PendingOnboardingNotificationOperations> pendingOnboardCaptor = ArgumentCaptor.forClass(PendingOnboardingNotificationOperations.class);
        verify(pendingOnboardingConnector, timeout(3000).times(1))
                .insert(pendingOnboardCaptor.capture());
        PendingOnboardingNotificationOperations captured = pendingOnboardCaptor.getValue();
        assertEquals(TestingProductUnavailableException.class.getSimpleName(), captured.getOnboardingFailure());
        verifyNoInteractions(internalApiConnector);
        verifyNoMoreInteractions(pendingOnboardingConnector);
    }

    private InstitutionOnboardedNotification returnNotificationMock(int bias) {
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification(), bias);
        notificationMock.setId(UUID.randomUUID().toString());
        InstitutionToNotify institution = mockInstance(new InstitutionToNotify(), bias);
        Billing billing = mockInstance(new Billing(), bias);
        notificationMock.setBilling(billing);
        notificationMock.setInstitution(institution);
        return notificationMock;
    }

    private OnboardingProductRequest returnRequestMock() {
        OnboardingProductRequest request = mockInstance(new OnboardingProductRequest());
        return request;
    }
}