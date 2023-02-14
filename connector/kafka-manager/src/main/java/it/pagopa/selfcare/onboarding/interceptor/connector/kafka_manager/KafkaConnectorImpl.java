package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConnectorImpl {

    @KafkaListener(topics = "${kafka-manager.onboarding-interceptor.topic}")
    public void intercept(ConsumerRecord<String, InstitutionOnboardedNotification> message) {
        log.debug("Incoming message: {}", message);
    }

}
