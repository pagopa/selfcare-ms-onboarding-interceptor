package it.pagopa.selfcare.onobarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.api.DaoConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onobarding.interceptor.connector.dao.model.PendingOnboardingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class DaoConnectorImpl implements DaoConnector {

    private final PendingOnboardingsRepository repository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DaoConnectorImpl(PendingOnboardingsRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public PendingOnboardingNotificationOperations insert(PendingOnboardingNotificationOperations entity) {
        PendingOnboardingEntity insert;
        final PendingOnboardingEntity pendingOnboarding = new PendingOnboardingEntity(entity);
        try {
            insert = repository.insert(pendingOnboarding);
        } catch (DuplicateKeyException e) {
            insert = repository.save(pendingOnboarding);
        }
        return insert;
    }


    @Override
    public PendingOnboardingNotificationOperations save(PendingOnboardingNotificationOperations entity) {
        final PendingOnboardingEntity productEntity = new PendingOnboardingEntity(entity);
        if (productEntity.getCreatedAt() == null) {
            productEntity.setCreatedAt(Instant.now());
        }
        return repository.save(productEntity);
    }


    @Override
    public Optional<PendingOnboardingNotificationOperations> findById(String id) {
        return repository.findById(id).map(Function.identity());
    }


    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }


    @Override
    public List<PendingOnboardingNotificationOperations> findAll() {
        return new ArrayList<>(repository.findAll());
    }


    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }


}
