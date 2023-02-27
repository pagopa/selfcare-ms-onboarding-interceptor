package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config.InstitutionOnboardingNotificationSerializer;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config.KafkaConsumerConfig;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboarded;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedBilling;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import it.pagopa.selfcare.onboarding.interceptor.model.product.ProductStatus;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {KafkaInterceptor.class, KafkaConsumerConfig.class})
@EmbeddedKafka(partitions = 1)
@DirtiesContext
@TestPropertySource(properties = {
        "onboarding-interceptor.products-allowed-list={'prod-interop':{'prod-interop-coll', 'prod-pn-coll'}}",
        "kafka-manager.onboarding-interceptor.topic=sc-contracts",
        "kafka-manager.onboarding-interceptor.bootstrapAddress=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka-manager.onboarding-interceptor.auto-offset-reset=earliest",
        "spring.cloud.stream.kafka.binder.zkNodes=${spring.embedded.zookeeper.connect}",
        "kafka-manager.onboarding-interceptor.groupId=consumer-test",
        "kafka-manager.onboarding-interceptor.security-protocol=PLAINTEXT"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaInterceptorTest {
    @SpyBean
    private KafkaInterceptor interceptor;
    @MockBean
    private PendingOnboardingConnector pendingOnboardingConnector;
    @MockBean
    private InternalApiConnector apiConnector;
    private Producer<String, InstitutionOnboardedNotification> producer;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private final ObjectMapper mapper;

    @Captor
    ArgumentCaptor<InstitutionOnboardedNotification> notificationArgumentCaptor;

    @Captor
    ArgumentCaptor<AutoApprovalOnboardingRequest> requestArgumentCaptor;

    @Captor
    ArgumentCaptor<PendingOnboardingNotificationOperations> pendingRequestCaptor;

    public KafkaInterceptorTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());

    }


    @BeforeAll
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<String, InstitutionOnboardedNotification>(configs, new StringSerializer(), new InstitutionOnboardingNotificationSerializer()).createProducer();
    }

    @Test
    void interceptKafkaMessage_Ok() throws ExecutionException, InterruptedException {
        //given
        InstitutionOnboardedNotification notificationMock = returnNotificationMock(0);
        notificationMock.setProduct("prod-interop");
        producer.send(new ProducerRecord<>("sc-contracts", notificationMock));
        producer.flush();
        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        Product productMockInterop = returnProductMock();
        productMockInterop.setId("prod-interop-coll");
        Product productMockPn = returnProductMock();
        productMockPn.setId("prod-pn-coll");

        when(apiConnector.getInstitutionById(anyString()))
                .thenReturn(institutionMock);
        when(apiConnector.getInstitutionProductUsers(anyString(), anyString()))
                .thenReturn(List.of(userMock));
        when(apiConnector.getProduct(anyString()))
                .thenReturn(productMockInterop)
                .thenReturn(productMockPn);

        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, times(1)).getInstitutionById(notificationMock.getInternalIstitutionID());
        verify(apiConnector, times(1)).getInstitutionProductUsers(notificationMock.getInternalIstitutionID(), notificationMock.getProduct());
        verify(apiConnector, times(1)).getProduct(productMockInterop.getId());
        verify(apiConnector, times(1)).getProduct(productMockPn.getId());
        verify(apiConnector, times(1)).autoApprovalOnboarding(eq(notificationMock.getInstitution().getTaxCode()), eq(productMockInterop.getId()), requestArgumentCaptor.capture());
        AutoApprovalOnboardingRequest request1 = requestArgumentCaptor.getValue();
        assertNotNull(request1);
        checkNotNullFields(request1.getPspData());
        checkNotNullFields(request1.getCompanyInformations());
        checkNotNullFields(request1.getAssistanceContacts());
        assertEquals(userMock, request1.getUsers().get(0));
        checkNotNullFields(request1.getBillingData());
        verifyNoInteractions(pendingOnboardingConnector);
        InstitutionOnboardedNotification capturedNotification = notificationArgumentCaptor.getValue();
        assertEquals(notificationMock, capturedNotification);
        producer.close();
    }

    @Test
    void interceptKafkaMessage_KoProductStatus() throws ExecutionException, InterruptedException {
        //given
        InstitutionOnboardedNotification notificationMock = returnNotificationMock(0);
        notificationMock.setProduct("prod-interop");
        producer.send(new ProducerRecord<>("sc-contracts", notificationMock));
        producer.flush();
        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        Product productMockInterop = returnProductMock();
        productMockInterop.setId("prod-interop-coll");
        productMockInterop.setStatus(ProductStatus.INACTIVE);
        Product productMockPn = returnProductMock();
        productMockPn.setId("prod-pn-coll");
        productMockPn.setStatus(ProductStatus.INACTIVE);

        when(apiConnector.getInstitutionById(anyString()))
                .thenReturn(institutionMock);
        when(apiConnector.getInstitutionProductUsers(anyString(), anyString()))
                .thenReturn(List.of(userMock));
        when(apiConnector.getProduct(anyString()))
                .thenReturn(productMockInterop)
                .thenReturn(productMockPn);

        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, times(1)).getInstitutionById(notificationMock.getInternalIstitutionID());
        verify(apiConnector, times(1)).getInstitutionProductUsers(notificationMock.getInternalIstitutionID(), notificationMock.getProduct());
        verify(apiConnector, times(1)).getProduct(productMockInterop.getId());
        InstitutionOnboardedNotification capturedNotification = notificationArgumentCaptor.getValue();
        assertEquals(notificationMock, capturedNotification);
        verify(pendingOnboardingConnector, times(1))
                .insert(pendingRequestCaptor.capture());
        verifyNoMoreInteractions(apiConnector);
        PendingOnboardingNotificationOperations captured = pendingRequestCaptor.getValue();
        assertEquals(captured.getNotification(), capturedNotification);
        checkNotNullFields(captured.getRequest());
        assertEquals(TestingProductUnavailableException.class.getSimpleName(), captured.getOnboardingFailure());
        producer.close();

    }


//    @Test
    void interceptKafkaMessage_KoOnboardingFailed() throws ExecutionException, InterruptedException {
        //given
        InstitutionOnboardedNotification notificationMock = returnNotificationMock(0);
        notificationMock.setProduct("prod-io");
        producer.send(new ProducerRecord<>("sc-contracts", notificationMock));
        producer.flush();
        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        Product productMockInterop = returnProductMock();
        productMockInterop.setId("prod-interop-coll");
        productMockInterop.setStatus(ProductStatus.INACTIVE);
        Product productMockPn = returnProductMock();
        productMockPn.setId("prod-pn-coll");
        productMockPn.setStatus(ProductStatus.INACTIVE);
        when(apiConnector.getInstitutionById(anyString()))
                .thenReturn(institutionMock);
        when(apiConnector.getInstitutionProductUsers(anyString(), anyString()))
                .thenReturn(List.of(userMock));
        when(apiConnector.getProduct(anyString()))
                .thenReturn(productMockInterop)
                .thenReturn(productMockPn);

        //then
        verify(interceptor, timeout(1000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, times(1)).getInstitutionById(notificationMock.getInternalIstitutionID());
        verify(apiConnector, times(1)).getInstitutionProductUsers(notificationMock.getInternalIstitutionID(), notificationMock.getProduct());
        InstitutionOnboardedNotification capturedNotification = notificationArgumentCaptor.getValue();
        assertEquals(notificationMock, capturedNotification);
        verify(pendingOnboardingConnector, times(1))
                .insert(pendingRequestCaptor.capture());
        verifyNoMoreInteractions(apiConnector);
        PendingOnboardingNotificationOperations captured = pendingRequestCaptor.getValue();
        assertEquals(captured.getNotification(), capturedNotification);
        checkNotNullFields(captured.getRequest());
        assertEquals(TestingProductUnavailableException.class.getSimpleName(), captured.getOnboardingFailure());
        producer.close();
    }

    @Test
    void interceptTestOnboarding() throws ExecutionException, InterruptedException {
        //given
        InstitutionOnboardedNotification notificationMock = returnNotificationMock(0);
        notificationMock.setProduct("prod-io-coll");
        producer.send(new ProducerRecord<>("sc-contracts", notificationMock));
        producer.flush();

        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        when(apiConnector.getInstitutionById(anyString()))
                .thenReturn(institutionMock);
        when(apiConnector.getInstitutionProductUsers(anyString(), anyString()))
                .thenReturn(List.of(userMock));
        when(pendingOnboardingConnector.insert(any(PendingOnboardingNotificationOperations.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, PendingOnboardingNotificationOperations.class));
        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        InstitutionOnboardedNotification capturedNotification = notificationArgumentCaptor.getValue();
        assertEquals(notificationMock, capturedNotification);

        verify(apiConnector, times(1)).getInstitutionById(notificationMock.getInternalIstitutionID());
        verify(apiConnector, times(1)).getInstitutionProductUsers(notificationMock.getInternalIstitutionID(), notificationMock.getProduct());
        verifyNoMoreInteractions(apiConnector);
        verifyNoInteractions(pendingOnboardingConnector);
        producer.close();

    }

    @Test
    void onboardingNotificationToAutoOnboardingRequest() throws IOException {
        //given
        File stub = ResourceUtils.getFile("classpath:stubs/KafkaInterceptorTest/Institution.json");
        Institution institution = mapper.readValue(stub, Institution.class);
        //when
        AutoApprovalOnboardingRequest request = KafkaInterceptor.ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST.apply(institution);
        //then
        assertEquals(institution.getGeographicTaxonomies(), request.getGeographicTaxonomies());
        assertEquals(institution.getInstitutionType(), request.getInstitutionType());
        assertEquals(institution.getOrigin(), request.getOrigin());
        assertEquals(institution.getAddress(), request.getBillingData().getRegisteredOffice());
        assertEquals(institution.getDescription(), request.getBillingData().getBusinessName());
        assertEquals(institution.getDigitalAddress(), request.getBillingData().getDigitalAddress());
        assertEquals(institution.getTaxCode(), request.getBillingData().getTaxCode());
        assertEquals(institution.getZipCode(), request.getBillingData().getZipCode());
        assertEquals(institution.getPaymentServiceProvider(), request.getPspData());
        assertEquals(institution.getCompanyInformations(), request.getCompanyInformations());
        assertEquals(institution.getAssistanceContacts(), request.getAssistanceContacts());
    }


    private InstitutionOnboardedNotification returnNotificationMock(int bias) {
        InstitutionOnboardedNotification notificationMock = mockInstance(new InstitutionOnboardedNotification(), bias);
        notificationMock.setId(UUID.randomUUID().toString());
        InstitutionOnboarded institution = mockInstance(new InstitutionOnboarded(), bias);
        InstitutionOnboardedBilling billing = mockInstance(new InstitutionOnboardedBilling(), bias);
        notificationMock.setBilling(billing);
        notificationMock.setInstitution(institution);
        return notificationMock;
    }

    private Institution returnIntitutionMock() {
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setAttributes(List.of(mockInstance(new Attribute())));
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setCompanyInformations(mockInstance(new CompanyInformations()));
        institutionMock.setDataProtectionOfficer(mockInstance(new DpoData()));
        institutionMock.setAssistanceContacts(mockInstance(new AssistanceContacts()));
        institutionMock.setPaymentServiceProvider(mockInstance(new PspData()));
        return institutionMock;
    }

    private User returnUserMock(int bias) {
        User userMock = mockInstance(new User(), bias);
        return userMock;
    }

    private Product returnProductMock() {
        Product product = mockInstance(new Product());
        return product;
    }
}