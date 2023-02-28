package it.pagopa.selfcare.onboarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.connector.dao.config.DaoTestConfig;
import it.pagopa.selfcare.onboarding.interceptor.connector.dao.model.PendingOnboardingEntity;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboarded;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedBilling;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {PendingOnboardingEntity.class, PendingOnboardingsRepository.class, DaoTestConfig.class})
class PendingOnboardingsRepositoryTest {

    @Autowired
    private PendingOnboardingsRepository repository;

    @Autowired
    private AuditorAware<String> auditorAware;

    @AfterEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    void create() {
        //given
        Instant now = Instant.now().minusSeconds(1);
        int bias = 0;
        PendingOnboardingEntity entity = returnMock(1);
        //when
        PendingOnboardingEntity savedEntity = repository.insert(entity);
        //then
        assertTrue(now.isBefore(savedEntity.getCreatedAt()));
        assertEquals(entity.getId(), savedEntity.getId());
    }

    @Test
    void findAll_atLeastOneRecord() {
        //given
        create();
        //when
        List<PendingOnboardingEntity> records = repository.findAll();
        //the
        assertTrue(records.size() > 0);
    }

    @Test
    void update() {
        //given
        String id = UUID.randomUUID().toString();
        PendingOnboardingEntity entity = returnMock(1);
        entity.setId(id);
        entity.setOnboardingFailure(OnboardingFailedException.class.getSimpleName());
        PendingOnboardingEntity savedEntity = repository.insert(entity);
        PendingOnboardingEntity entity1 = returnMock(2);
        entity1.setId(id);
        entity1.setNew(false);
        entity1.setOnboardingFailure(TestingProductUnavailableException.class.getSimpleName());
        //when
        PendingOnboardingEntity modifiedEntity = repository.save(entity1);
        //then
        assertEquals(entity.getId(), modifiedEntity.getId());
        assertEquals(entity1.getNotification(), modifiedEntity.getNotification());
        assertNotEquals(savedEntity.getNotification(), modifiedEntity.getNotification());
        assertNotEquals(savedEntity.getOnboardingFailure(), modifiedEntity.getOnboardingFailure());
        assertTrue(savedEntity.getModifiedAt().isBefore(modifiedEntity.getModifiedAt()));
    }

    private PendingOnboardingEntity returnMock(int bias) {
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification(), bias);
        notificationMock.setId(UUID.randomUUID().toString());
        InstitutionOnboarded institution = mockInstance(new InstitutionOnboarded(), bias);
        InstitutionOnboardedBilling billing = mockInstance(new InstitutionOnboardedBilling(), bias);
        notificationMock.setBilling(billing);
        notificationMock.setInstitution(institution);
        AutoApprovalOnboardingRequest requestMock = mockInstance(new AutoApprovalOnboardingRequest(), bias);
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