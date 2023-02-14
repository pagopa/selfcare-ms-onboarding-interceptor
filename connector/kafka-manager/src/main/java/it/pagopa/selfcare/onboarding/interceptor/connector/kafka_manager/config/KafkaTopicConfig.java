package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:config/kafka-manager.properties")
public class KafkaTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${kafka-manager.onboarding-interceptor.topic}")
    private String topic;

    @Value(value = "${kafka-manager.onboarding-interceptor.security-protocol}")
    private String securityProtocol;

    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-mechanism}")
    private String saslMechanism;

    @Value(value = "${kafka-manager.onboarding-interceptor.sasl-config}")
    private String saslConfig;


//    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, bootstrapAddress);
        configs.put("sasl.mechanism", saslMechanism);
        configs.put("sasl.jaas.config", saslConfig);
        configs.put("security.protocol", securityProtocol);
        return new KafkaAdmin(configs);
    }

    //    @Bean
    public NewTopic topic1() {
        return new NewTopic(topic, 1, (short) 1);
    }
}
