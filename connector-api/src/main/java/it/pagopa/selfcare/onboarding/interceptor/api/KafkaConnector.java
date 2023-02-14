package it.pagopa.selfcare.onboarding.interceptor.api;

public interface KafkaConnector<T> {
     void getMessages();

//     T getMessages();
}
