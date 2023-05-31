package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.onboarding.interceptor.api.ExceptionDaoConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.config.KafkaConsumerConfig;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.OnboardingFailedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.TestingProductUnavailableException;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.*;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboarded;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedBilling;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import it.pagopa.selfcare.onboarding.interceptor.model.onboarding.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {KafkaInterceptor.class, KafkaConsumerConfig.class})
@EmbeddedKafka(partitions = 1, controlledShutdown = true)
@DirtiesContext
@TestPropertySource(properties = {
        "onboarding-interceptor.products-allowed-list={'prod-interop':{'prod-interop-coll'}}",
        "kafka-manager.onboarding-interceptor.topic=sc-contracts",
        "kafka-manager.onboarding-interceptor.bootstrapAddress=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka-manager.onboarding-interceptor.auto-offset-reset=earliest",
        "spring.cloud.stream.kafka.binder.zkNodes=${spring.embedded.zookeeper.connect}",
        "kafka-manager.onboarding-interceptor.groupId=consumer-test",
        "kafka-manager.onboarding-interceptor.security-protocol=PLAINTEXT"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Profile("KafkaInterceptor")
@ContextConfiguration(classes = {KafkaInterceptorTest.Config.class})
class KafkaInterceptorTest {

    public static class Config {
        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.registerModule(new Jdk8Module());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setTimeZone(TimeZone.getDefault());
            return mapper;
        }
    }
    @SpyBean
    private KafkaInterceptor interceptor;
    @MockBean
    private PendingOnboardingConnector pendingOnboardingConnector;
    @MockBean
    private InternalApiConnector apiConnector;
    @MockBean
    private ExceptionDaoConnector exceptionDaoConnector;
    @MockBean
    OnboardingValidationStrategy validationStrategy;

    private Optional<Map<String, Set<String>>> allowedProductsMap = Optional.of(Map.of("prod-interop", Set.of("prod-interop-coll")));

    private Producer<String, String> producer;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper mapper;

    @Captor
    ArgumentCaptor<ConsumerRecord<String, String>> notificationArgumentCaptor;

    @Captor
    ArgumentCaptor<AutoApprovalOnboardingRequest> requestArgumentCaptor;

    @Captor
    ArgumentCaptor<PendingOnboardingNotificationOperations> pendingRequestCaptor;

    @Captor
    ArgumentCaptor<String> recordValueArgumentCaptor;

    @Captor
    ArgumentCaptor<Exception> exceptionArgumentCaptor;

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


    @BeforeEach
    void setUp() throws InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<String, String>(configs).createProducer();
        reset(interceptor, apiConnector, pendingOnboardingConnector, validationStrategy);
        Thread.sleep(1000);
    }

    @Test
    void interceptKafkaMessage_Ok() throws JsonProcessingException {
        //given
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-interop");

        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        String prodInteropCollId = "prod-interop-coll";
        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        doReturn(true)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        producer.send(new ProducerRecord<>("sc-contracts", mapper.writeValueAsString(notificationPayload)));
        producer.flush();
        //then
        verify(interceptor, timeout(1000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionProductUsers(notificationPayload.getInternalIstitutionID(), notificationPayload.getProduct());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionById(notificationPayload.getInternalIstitutionID());
        verify(validationStrategy, timeout(1000).times(1)).validate(any(), eq(allowedProductsMap));
        verify(apiConnector, timeout(1000).times(1)).autoApprovalOnboarding(eq(notificationPayload.getInstitution().getTaxCode()), eq(prodInteropCollId), requestArgumentCaptor.capture());
        AutoApprovalOnboardingRequest request1 = requestArgumentCaptor.getValue();
        assertNotNull(request1);
        checkNotNullFields(request1.getPspData());
        checkNotNullFields(request1.getCompanyInformations());
        checkNotNullFields(request1.getAssistanceContacts());
        assertEquals(userMock, request1.getUsers().get(0));
        checkNotNullFields(request1.getBillingData());
        verifyNoMoreInteractions(validationStrategy);
        verifyNoMoreInteractions(apiConnector);
        verifyNoInteractions(pendingOnboardingConnector);
        ConsumerRecord<String, String> capturedNotification = notificationArgumentCaptor.getValue();
        reflectionEqualsByName(notificationPayload, capturedNotification, "updatedAt");

    }

    @Test
    void interceptKafkaMessage_KoProduct() throws JsonProcessingException {
        //given
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-interop");

        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        doThrow(TestingProductUnavailableException.class)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        producer.send(new ProducerRecord<>("sc-contracts", mapper.writeValueAsString(notificationPayload)));
        producer.flush();
        //then
        verify(interceptor, timeout(1000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionById(notificationPayload.getInternalIstitutionID());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionProductUsers(notificationPayload.getInternalIstitutionID(), notificationPayload.getProduct());
        verify(validationStrategy, timeout(1000).times(1)).validate(any(), eq(allowedProductsMap));
        ConsumerRecord<String, String> capturedNotification = notificationArgumentCaptor.getValue();
        reflectionEqualsByName(notificationPayload, capturedNotification, "updatedAt");
        verify(pendingOnboardingConnector, timeout(2000).times(1))
                .insert(pendingRequestCaptor.capture());
        verifyNoMoreInteractions(apiConnector);
        PendingOnboardingNotificationOperations captured = pendingRequestCaptor.getValue();
        assertEquals(captured.getNotification(), mapper.readValue(capturedNotification.value(), InstitutionOnboardedNotification.class));
        checkNotNullFields(captured.getRequest());
        assertEquals(TestingProductUnavailableException.class.getSimpleName(), captured.getOnboardingFailure());

    }

    @Test
    void testTimeConversion() throws JsonProcessingException {
        OffsetDateTime updatedAt = mapper.readValue("2022-10-26T07:58:42.143", OffsetDateTime.class);
    }

    @Test
    void interceptKafkaMessage_KoOnboardingFailed() throws JsonProcessingException {
        //given
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-io");

        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);

        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        doThrow(OnboardingFailedException.class)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        producer.send(new ProducerRecord<>("sc-contracts", mapper.writeValueAsString(notificationPayload)));
        producer.flush();
        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionById(notificationPayload.getInternalIstitutionID());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionProductUsers(notificationPayload.getInternalIstitutionID(), notificationPayload.getProduct());
        verify(validationStrategy, timeout(1000).times(1)).validate(any(), eq(allowedProductsMap));
        ConsumerRecord<String, String> capturedNotification = notificationArgumentCaptor.getValue();
        reflectionEqualsByName(notificationPayload, capturedNotification, "updatedAt");
        verify(pendingOnboardingConnector, timeout(2000).times(1))
                .insert(pendingRequestCaptor.capture());
        verifyNoMoreInteractions(apiConnector);
        PendingOnboardingNotificationOperations captured = pendingRequestCaptor.getValue();
        checkNotNullFields(captured.getRequest());
        assertEquals(OnboardingFailedException.class.getSimpleName(), captured.getOnboardingFailure());
    }

    @Test
    void interceptTestOnboarding() throws JsonProcessingException {
        //given
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-io-coll");
        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);
        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        when(validationStrategy.validate(any(), any()))
                .thenReturn(false);
        //when
        producer.send(new ProducerRecord<>("sc-contracts", mapper.writeValueAsString(notificationPayload)));
        producer.flush();
        //then
        verify(interceptor, timeout(1000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        ConsumerRecord<String, String> capturedNotification = notificationArgumentCaptor.getValue();
        reflectionEqualsByName(notificationPayload, capturedNotification, "updatedAt");

        verify(apiConnector, timeout(1000).times(1)).getInstitutionById(notificationPayload.getInternalIstitutionID());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionProductUsers(notificationPayload.getInternalIstitutionID(), notificationPayload.getProduct());
        verify(validationStrategy, times(1)).validate(any(), eq(allowedProductsMap));
        verifyNoMoreInteractions(apiConnector);
        verifyNoMoreInteractions(validationStrategy);
        verifyNoInteractions(pendingOnboardingConnector);

    }

    @Test
    void intercept_AlreadyOnboardedInstitutionException() throws JsonProcessingException {
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-interop");
        String prodInteropCollId = "prod-interop-coll";
        Institution institutionMock = returnIntitutionMock();
        User userMock = returnUserMock(1);

        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        doReturn(true)
                .when(validationStrategy)
                .validate(any(), any());
        doThrow(InstitutionAlreadyOnboardedException.class)
                .when(apiConnector)
                .autoApprovalOnboarding(anyString(), any(), any());
        //when
        producer.send(new ProducerRecord<>("sc-contracts", mapper.writeValueAsString(notificationPayload)));
        producer.flush();
        //then
        verify(interceptor, timeout(5000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        ConsumerRecord<String, String> capturedNotification = notificationArgumentCaptor.getValue();
        reflectionEqualsByName(notificationPayload, capturedNotification, "updatedAt");

        verify(apiConnector, timeout(1000).times(1)).getInstitutionById(notificationPayload.getInternalIstitutionID());
        verify(apiConnector, timeout(1000).times(1)).getInstitutionProductUsers(notificationPayload.getInternalIstitutionID(), notificationPayload.getProduct());
        verify(validationStrategy, times(1)).validate(any(), eq(allowedProductsMap));
        verify(apiConnector, times(1)).autoApprovalOnboarding(eq(notificationPayload.getInstitution().getTaxCode()), eq(prodInteropCollId), requestArgumentCaptor.capture());
        AutoApprovalOnboardingRequest request1 = requestArgumentCaptor.getValue();
        assertNotNull(request1);
        verifyNoMoreInteractions(apiConnector);
        verifyNoMoreInteractions(validationStrategy);
        verifyNoInteractions(pendingOnboardingConnector);
    }

    @Test
    void intercept_serializationException() throws JsonProcessingException {
        InstitutionOnboardedNotification notificationPayload = returnNotificationMock(0);
        notificationPayload.setProduct("prod-interop");
        String payload = mapper.writeValueAsString(notificationPayload);
        String newPayload = payload.replace("PA", "TEST");
        Institution institutionMock = returnIntitutionMock();

        User userMock = returnUserMock(1);
        doReturn(institutionMock)
                .when(apiConnector)
                .getInstitutionById(anyString());
        doReturn(List.of(userMock))
                .when(apiConnector)
                .getInstitutionProductUsers(anyString(), anyString());
        doThrow(TestingProductUnavailableException.class)
                .when(validationStrategy)
                .validate(any(), any());
        //when
        producer.send(new ProducerRecord<>("sc-contracts", newPayload));
        producer.flush();
        //then
        verify(interceptor, timeout(1000).times(1))
                .intercept(notificationArgumentCaptor.capture());
        verify(exceptionDaoConnector, timeout(1000).times(1)).insert(recordValueArgumentCaptor.capture(), exceptionArgumentCaptor.capture());
        String capturedRecord = recordValueArgumentCaptor.getValue();
        assertEquals(newPayload, capturedRecord);
        verifyNoMoreInteractions(exceptionDaoConnector);
        verifyNoInteractions(pendingOnboardingConnector, apiConnector, validationStrategy);
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
        assertEquals(institution.getDataProtectionOfficer(), request.getPspData().getDpoData());
    }

    @Test
    void onboardingNotificationToAutoOnbaordingRequest_noPSPData() throws IOException {
        //given
        File stub = ResourceUtils.getFile("classpath:stubs/KafkaInterceptorTest/InstitutionNoPsp.json");
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
        assertEquals(institution.getCompanyInformations(), request.getCompanyInformations());
        assertEquals(institution.getAssistanceContacts(), request.getAssistanceContacts());
        assertNull(request.getPspData());

        institution.setGeographicTaxonomies(null);

        request = KafkaInterceptor.ONBOARDING_NOTIFICATION_TO_AUTO_APPROVAL_REQUEST.apply(institution);
        assertTrue(request.getGeographicTaxonomies().isEmpty());
    }

    @AfterEach
    void shutdown() {
        producer.close();
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
        DpoData dpo = mockInstance(new DpoData());
        institutionMock.setDataProtectionOfficer(dpo);
        institutionMock.setAssistanceContacts(mockInstance(new AssistanceContacts()));
        institutionMock.setPaymentServiceProvider(mockInstance(new PspData()));
        institutionMock.getPaymentServiceProvider().setDpoData(dpo);
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