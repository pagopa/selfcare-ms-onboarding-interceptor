package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.api.KafkaConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Service
public class KafkaConnectorImpl implements KafkaConnector<InstitutionOnboardedNotification>, Runnable {

    //    @Value("${kafka-manager.onboarding-interceptor.groupId}")
//    private String groupId;
//    @Value(value = "${spring.kafka.bootstrap-servers}")
//    private String bootstrapAddress;
//
    private String topic;
//
//    @Value(value = "${kafka-manager.onboarding-interceptor.security-protocol}")
//    private String securityProtocol;
//
//    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-mechanism}")
//    private String saslMechanism;
//
//    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-config}")
//    private String saslConfig;

    private final KafkaConsumer<String, InstitutionOnboardedNotification> kafkaConsumer;

    @Autowired
    public KafkaConnectorImpl(KafkaConsumer<String, InstitutionOnboardedNotification> kafkaConsumer,
                              @Value(value = "${kafka-manager.onboarding-interceptor.topic}")
                              String topic) {
        this.kafkaConsumer = kafkaConsumer;
        this.run();
        this.topic = topic;
    }

    @KafkaListener(topics = "SC-Contracts",
            containerFactory = "onboardedInstitutionKafkaListenerContainerFactory")
    private void intercept(InstitutionOnboardedNotification message) {
        log.debug("Incoming message: {}", message);
    }

    public void getMessages() {
        kafkaConsumer.subscribe(Collections.singletonList(topic));
        while (true) {
            ConsumerRecords<String, InstitutionOnboardedNotification> records =
                    kafkaConsumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, InstitutionOnboardedNotification> record : records) {
                log.info("Key: " + record.key() + ", Value: " + record.value());
                log.info("Partition: " + record.partition() + ", Offset:" + record.offset());
            }
        }
    }

    @Override
    public void run() {
        getMessages();
    }

}
