package it.pagopa.selfcare.onboarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.dao.model.PendingOnboardingEntity;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class PendingOnboardingConnectorImpl implements PendingOnboardingConnector {

    private final PendingOnboardingsRepository repository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public PendingOnboardingConnectorImpl(PendingOnboardingsRepository repository,
                                          MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public PendingOnboardingNotificationOperations insert(PendingOnboardingNotificationOperations entity) {
        log.debug("PendingOnboardingNotificationOperations insert = {}", entity);
        PendingOnboardingEntity insert;
        final PendingOnboardingEntity pendingOnboarding = new PendingOnboardingEntity(entity);
        try {
            insert = repository.insert(pendingOnboarding);
            log.debug("Saved pendingRequest = {}", insert);
        } catch (DuplicateKeyException e) {
            pendingOnboarding.setNew(false);
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
    public PendingOnboardingNotificationOperations findOldest() {
        log.trace("PendingOnboardingNotificationOperations findOldest start");
        Query query = new Query();
        query.limit(1);
        query.with(Sort.by(Sort.Direction.ASC, "createdAt"));
        query.addCriteria(Criteria.where("notification.product").is("prod-interop"));
        PendingOnboardingEntity pendingOnboarding = mongoTemplate.findAndModify(query,
                Update.update(PendingOnboardingEntity.Fields.createdAt, Instant.now()),
                PendingOnboardingEntity.class);
        log.debug("PendingOnboardingNotificationOperations findOldest result = {}", pendingOnboarding);
        log.trace("PendingOnboardingNotificationOperations findOldest end");
        return pendingOnboarding;
    }


    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }


}
