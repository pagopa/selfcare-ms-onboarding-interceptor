package it.pagopa.selfcare.onboarding.interceptor.model.onboarding;

import java.time.OffsetDateTime;

public interface ExceptionOperations {
    String getId();
    void setId(String id);
    OffsetDateTime getCreatedAt();
    void setCreatedAt(OffsetDateTime createdAt);
    OffsetDateTime getUpdatedAt();
    void setUpdatedAt(OffsetDateTime updatedAt);
    String getNotification();
    void setNotification(String notification);
    String getExceptionDescription();
    void setExceptionDescription(String exceptionDescription);
}
