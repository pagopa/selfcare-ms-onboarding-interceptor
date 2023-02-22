package it.pagopa.selfcare.onobarding.interceptor.connector.dao;


import it.pagopa.selfcare.onobarding.interceptor.connector.dao.model.PendingOnboardingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PendingOnboardingsRepository extends MongoRepository<PendingOnboardingEntity, String> {
}
