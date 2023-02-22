package it.pagopa.selfcare.onobarding.interceptor.connector.dao.model;

import it.pagopa.selfcare.onboarding.interceptor.api.PendingOnboardingNotificationOperations;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.kafka.InstitutionOnboardedNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Document("pendingOnboardings")
public class PendingOnboardingEntity implements PendingOnboardingNotificationOperations, Persistable<String> {
    public PendingOnboardingEntity(PendingOnboardingNotificationOperations entity) {
        this();
        id = entity.getNotification().getId();
        createdAt = entity.getCreatedAt();
        request = entity.getRequest();
        notification = entity.getNotification();
    }

    @Id
    private String id;
    @CreatedDate
    private Instant createdAt;

    private AutoApprovalOnboardingRequest request;

    private InstitutionOnboardedNotification notification;

    @Transient
    private boolean isNew = true;
}
