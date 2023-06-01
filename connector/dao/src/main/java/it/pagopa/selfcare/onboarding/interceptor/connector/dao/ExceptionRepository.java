package it.pagopa.selfcare.onboarding.interceptor.connector.dao;

import it.pagopa.selfcare.onboarding.interceptor.connector.dao.model.ExceptionsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExceptionRepository extends MongoRepository<ExceptionsEntity, String> {
}
