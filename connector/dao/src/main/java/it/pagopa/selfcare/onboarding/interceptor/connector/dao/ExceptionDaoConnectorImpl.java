package it.pagopa.selfcare.onboarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.api.ExceptionDaoConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.dao.model.ExceptionsEntity;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.ExceptionOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ExceptionDaoConnectorImpl implements ExceptionDaoConnector {

    private final ExceptionRepository repository;


    @Autowired
    public ExceptionDaoConnectorImpl(ExceptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExceptionOperations insert(String value, Exception exception) {
        log.trace("insert start");
        log.debug("insert record value = {}. exception = {}", value, exception);
        ExceptionsEntity insert;
        final ExceptionsEntity entity = new ExceptionsEntity();
        String id = value.substring(6, 43);
        entity.setId(id);
        entity.setNotification(value);
        entity.setExceptionDescription(exception.getMessage());
        entity.setException(exception.getClass().getSimpleName());
        try{
            insert = repository.insert(entity);
            log.debug("saved exception = {}", insert);
        } catch (DuplicateKeyException e){
            entity.setNew(false);
            insert = repository.save(entity);
            log.debug("updated exception = {}", insert);
        }
        return insert;
    }

    @Override
    public ExceptionOperations save(ExceptionOperations exceptionOperations) {
        final ExceptionsEntity exceptionsEntity = new ExceptionsEntity(exceptionOperations);
        if (exceptionsEntity.getCreatedAt() == null){
            exceptionsEntity.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(exceptionsEntity);
    }
}
