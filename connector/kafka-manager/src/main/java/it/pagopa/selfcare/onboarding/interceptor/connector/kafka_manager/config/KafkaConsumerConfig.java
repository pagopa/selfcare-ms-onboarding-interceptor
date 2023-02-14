package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config;

import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model.InstitutionOnboardedNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@PropertySource("classpath:config/kafka-manager.properties")
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${kafka-manager.onboarding-interceptor.groupId}")
    private String groupId;
    @Value(value = "${kafka-manager.onboarding-interceptor.bootstrapAddress}")
    private String bootstrapAddress;
    @Value(value = "${kafka-manager.onboarding-interceptor.topic}")
    private String topic;
    @Value(value = "${kafka-manager.onboarding-interceptor.security-protocol}")
    private String securityProtocol;
    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-mechanism}")
    private String saslMechanism;
    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-config}")
    private String saslConfig;

    @Bean
    public ConsumerFactory<String, InstitutionOnboardedNotification> onboardedInstitutionConsumerFactory() {
        log.trace("Initializing {}", KafkaConsumerConfig.class.getSimpleName());
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + System.currentTimeMillis());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "onboarding-interceptor");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, saslConfig);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InstitutionOnboardedNotification>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InstitutionOnboardedNotification> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        //set concurrency number
        factory.setConsumerFactory(onboardedInstitutionConsumerFactory());
        factory.setConcurrency(2);//set environment variable
        //possible to change CK mode
        return factory;
    }

}
