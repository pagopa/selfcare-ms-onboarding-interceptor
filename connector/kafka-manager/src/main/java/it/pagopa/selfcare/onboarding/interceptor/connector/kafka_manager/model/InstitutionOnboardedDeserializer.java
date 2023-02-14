package it.pagopa.selfcare.onboarding.interceptor.connector.kafka_manager.model;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class InstitutionOnboardedDeserializer implements DeserializationSchema<InstitutionOnboardedNotification> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final long serialVersionUID = 1L;

    @Override
    public InstitutionOnboardedNotification deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, InstitutionOnboardedNotification.class);
    }

    @Override
    public boolean isEndOfStream(InstitutionOnboardedNotification onboarding) {
        return false;
    }

    @Override
    public TypeInformation<InstitutionOnboardedNotification> getProducedType() {
        return TypeInformation.of(InstitutionOnboardedNotification.class);
    }

}
