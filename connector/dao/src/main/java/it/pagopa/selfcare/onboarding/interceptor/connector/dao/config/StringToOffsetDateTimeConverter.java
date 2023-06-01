package it.pagopa.selfcare.onboarding.interceptor.connector.dao.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(String source) {
        return OffsetDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
