package it.pagopa.selfcare.onboarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.connector.dao.model.PendingOnboardingEntity;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionToNotify;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//TODO: update tests once implemented what's proposed on the implementation class
public class PendingOnboardingConnectorImplTest {

    private final PendingOnboardingsRepository repositoryMock;
    private final MongoTemplate mongoTemplateMock;
    private final PendingOnboardingConnectorImpl pendingOnboardingConnector;

    public PendingOnboardingConnectorImplTest() {
        this.repositoryMock = Mockito.mock(PendingOnboardingsRepository.class);
        this.mongoTemplateMock = Mockito.mock(MongoTemplate.class);
        this.pendingOnboardingConnector = new PendingOnboardingConnectorImpl(repositoryMock, mongoTemplateMock);
    }

    @AfterEach
    void clear() {
        repositoryMock.deleteAll();
        Mockito.reset(repositoryMock, mongoTemplateMock);
    }

    @Test
    void insert() {
        // given
        PendingOnboardingEntity entity = returnMock(1);
        when(repositoryMock.insert(any(PendingOnboardingEntity.class)))
                .thenReturn(entity);
        // when
        PendingOnboardingNotificationOperations saved = pendingOnboardingConnector.insert(entity);
        // then
        assertEquals(entity, saved);
        verify(repositoryMock, times(1))
                .insert(entity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void insert_duplicateKey() {
        // given
        PendingOnboardingEntity entity = returnMock(1);
        doThrow(DuplicateKeyException.class)
                .when(repositoryMock)
                .insert(any(PendingOnboardingEntity.class));
        // when
        Executable executable = () -> pendingOnboardingConnector.insert(entity);
        // then
        assertDoesNotThrow(executable);
        verify(repositoryMock, times(1))
                .insert(entity);
        verify(repositoryMock, times(1))
                .save(entity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void save_getCreatedAtNotNull() {
        // given
        PendingOnboardingEntity entity = returnMock(1);
        entity.setCreatedAt(Instant.now().minusSeconds(1));
        assertNotNull(entity.getCreatedAt());
        when(repositoryMock.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, PendingOnboardingEntity.class));
        // when
        PendingOnboardingNotificationOperations saved = pendingOnboardingConnector.save(entity);
        // then
        assertEquals(entity, saved);
        assertNotNull(saved.getCreatedAt());
        verify(repositoryMock, times(1))
                .save(entity);
        verifyNoMoreInteractions(repositoryMock);
    }


    @Test
    void save_getCreatedAtNull() {
        // given
        PendingOnboardingEntity entity = returnMock(1);
        assertNull(entity.getCreatedAt());
        when(repositoryMock.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, PendingOnboardingEntity.class));
        // when
        PendingOnboardingNotificationOperations saved = pendingOnboardingConnector.save(entity);
        // then
        assertEquals(entity, saved);
        assertNotNull(saved.getCreatedAt());
        verify(repositoryMock, times(1))
                .save(entity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findById() {
        // given
        String id = "id";
        Optional<PendingOnboardingEntity> entity = Optional.of(returnMock(1));
        when(repositoryMock.findById(any()))
                .thenReturn(entity);
        // when
        Optional<PendingOnboardingNotificationOperations> found = pendingOnboardingConnector.findById(id);
        // then
        assertEquals(entity, found);
        verify(repositoryMock, times(1))
                .findById(id);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void existsById() {
        // given
        String id = "id";
        boolean expected = true;
        when(repositoryMock.existsById(any()))
                .thenReturn(expected);
        // when
        boolean exists = pendingOnboardingConnector.existsById(id);
        // then
        assertEquals(expected, exists);
        verify(repositoryMock, times(1))
                .existsById(id);
        verifyNoMoreInteractions(repositoryMock);
    }


    @Test
    void findAll() {
        // given
        List<PendingOnboardingEntity> expected = List.of(returnMock(1));
        when(repositoryMock.findAll())
                .thenReturn(expected);
        // when
        List<PendingOnboardingNotificationOperations> found = pendingOnboardingConnector.findAll();
        // then
        assertEquals(expected, found);
        verify(repositoryMock, times(1))
                .findAll();
        verifyNoMoreInteractions(repositoryMock);
    }


    @Test
    void deleteById() {
        // given
        String id = "id";
        Mockito.doNothing()
                .when(repositoryMock)
                .deleteById(any());
        // when
        pendingOnboardingConnector.deleteById(id);
        // then
        verify(repositoryMock, times(1))
                .deleteById(id);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findOldest() {
        //given
        PendingOnboardingEntity pendingOnboarding = mockInstance(new PendingOnboardingEntity());
        when(mongoTemplateMock.findAndModify(any(Query.class), any(Update.class), (Class<PendingOnboardingEntity>) any()))
                .thenReturn(pendingOnboarding);
        //when
        PendingOnboardingNotificationOperations oldest = pendingOnboardingConnector.findOldest();
        //then
        assertEquals(pendingOnboarding, oldest);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, times(1))
                .findAndModify(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        assertTrue(query.getQueryObject().containsKey("notification.product"));
        assertEquals("prod-interop", query.getQueryObject().get("notification.product"));
        assertTrue(query.isSorted());
        assertTrue(query.getSortObject().containsKey("createdAt"));
    }

    private PendingOnboardingEntity returnMock(int bias) {
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification(), bias);
        notificationMock.setId(UUID.randomUUID().toString());
        InstitutionToNotify institution = mockInstance(new InstitutionToNotify(), bias);
        Billing billing = mockInstance(new Billing(), bias);
        notificationMock.setBilling(billing);
        notificationMock.setInstitution(institution);
        OnboardingProductRequest requestMock = mockInstance(new OnboardingProductRequest(), bias);
        requestMock.setAssistanceContacts(mockInstance(new AssistanceContacts(), bias));
        requestMock.setBillingData(mockInstance(new BillingData(), bias));
        requestMock.setUsers(List.of(mockInstance(new User(), bias)));
        requestMock.setCompanyInformations(mockInstance(new CompanyInformations(), bias));
        requestMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy(), bias)));
        PendingOnboardingEntity entity = mockInstance(new PendingOnboardingEntity(), "setCreatedAt");
        entity.setId(notificationMock.getId());
        entity.setNotification(notificationMock);
        entity.setRequest(requestMock);

        return entity;
    }
}
