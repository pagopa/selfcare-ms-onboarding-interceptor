package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;


import com.fasterxml.jackson.databind.ObjectMapper;

public class InstitutionOnboardedDeserializer {
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final long serialVersionUID = 1L;

//    @Override
//    public InstitutionOnboardedNotification deserialize(byte[] bytes) throws IOException {
//        return objectMapper.readValue(bytes, InstitutionOnboardedNotification.class);
//    }
//
//    @Override
//    public boolean isEndOfStream(InstitutionOnboardedNotification onboarding) {
//        return false;
//    }

//    @Override
//    public TypeInformation<InstitutionOnboardedNotification> getProducedType() {
//        return TypeInformation.of(InstitutionOnboardedNotification.class);
//    }

}
