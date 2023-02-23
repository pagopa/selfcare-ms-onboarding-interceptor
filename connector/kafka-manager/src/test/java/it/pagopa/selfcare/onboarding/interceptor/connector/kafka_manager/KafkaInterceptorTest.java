package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config.InstitutionOnboardingNotificationSerializer;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboarded;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedBilling;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}", classes = KafkaInterceptor.class)
@EmbeddedKafka
@DirtiesContext
@TestPropertySource(properties = {
        "onboarding-interceptor.products-allowed-list={'prod-interop':{'prod-interop-coll', 'prod-pn-coll'}}"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaInterceptorTest {
    @SpyBean
    private KafkaInterceptor interceptor;
    @MockBean
    private PendingOnboardingConnector onboardingConnector;

    @MockBean
    private InternalApiConnector apiConnector;


    private Producer<String, InstitutionOnboardedNotification> producer;


    private String topic = "SC-Contracts";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Captor
    ArgumentCaptor<InstitutionOnboardedNotification> notificationArgumentCaptor;

    @Captor
    ArgumentCaptor<Long> offsetArgumentCaptor;

    @BeforeAll
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<String, InstitutionOnboardedNotification>(configs, new StringSerializer(), new InstitutionOnboardingNotificationSerializer()).createProducer();
    }

    //    @Test
    void interceptKafkaMessage() {
        //given
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification());
        notificationMock.setProduct("prod-interop");
        InstitutionOnboarded onboardedInstitutionMock = mockInstance(new InstitutionOnboarded());
        InstitutionOnboardedBilling institutionOnboardedBillingMock = mockInstance(new InstitutionOnboardedBilling());
        notificationMock.setInstitution(onboardedInstitutionMock);
        notificationMock.setBilling(institutionOnboardedBillingMock);
        producer.send(new ProducerRecord<>(topic, notificationMock));
        producer.flush();
        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());

        InstitutionOnboardedNotification capturedNotification = notificationArgumentCaptor.getValue();
        assertEquals(notificationMock, capturedNotification);

    }

    @AfterAll
    void shutdown() {
        producer.close();
    }
}