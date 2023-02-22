package it.pagopa.selfcare.onobarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboarded;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedBilling;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onobarding.interceptor.connector.dao.config.DaoTestConfig;
import it.pagopa.selfcare.onobarding.interceptor.connector.dao.model.PendingOnboardingEntity;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification());
        notificationMock.setId(UUID.randomUUID().toString());
        InstitutionOnboarded institution = mockInstance(new InstitutionOnboarded());
        InstitutionOnboardedBilling billing = mockInstance(new InstitutionOnboardedBilling());
        notificationMock.setBilling(billing);
        notificationMock.setInstitution(institution);
        AutoApprovalOnboardingRequest requestMock = mockInstance(new AutoApprovalOnboardingRequest());
        requestMock.setAssistanceContacts(mockInstance(new AssistanceContacts()));
        requestMock.setBillingData(mockInstance(new BillingData()));
        requestMock.setUsers(List.of(mockInstance(new User())));
        requestMock.setCompanyInformations(mockInstance(new CompanyInformations()));
        requestMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        PendingOnboardingEntity entity = mockInstance(new PendingOnboardingEntity(), "setCreatedAt");
        entity.setId(notificationMock.getId());
        entity.setNotification(notificationMock);
        entity.setRequest(requestMock);
        //when
        PendingOnboardingEntity savedEntity = repository.insert(entity);
        //then
        assertTrue(now.isBefore(savedEntity.getCreatedAt()));
        assertEquals(entity.getId(), savedEntity.getId());
    }

}