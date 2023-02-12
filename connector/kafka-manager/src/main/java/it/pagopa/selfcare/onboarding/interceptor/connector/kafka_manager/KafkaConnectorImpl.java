package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config.KafkaConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConnectorImpl implements KafkaConnector {

//    private final static String[] TOPIC;

//    @Autowired
//    public KafkaConnectorImpl(@Value("${kafka-manager.onboarding-interceptor.topic}")String[] topic) {
//        TOPIC = topic;
//    }

    @Override
    @KafkaListener(topics = "SC-Contracts")
    public void intercept(InstitutionOnboardedNotification message) {
        log.debug("Incoming message: {}", message);
    }
}
