package it.pagopa.selfcare.onboarding.interceptor.connector.dao.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

class StringToOffsetDateTimeConverterTest {

    @Autowired
    private StringToOffsetDateTimeConverter  converter;

    @Test
    void testOffsetDateTimeConverter(){
        String date = "2023-05-22T10:34:10.931405";
        OffsetDateTime convert = converter.convert(date);

    }

}