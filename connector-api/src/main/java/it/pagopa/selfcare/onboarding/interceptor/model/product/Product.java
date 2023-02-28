package it.pagopa.selfcare.onboarding.interceptor.model.product;

import lombok.Data;

@Data
public class Product {

    private String id;
    private String title;
    private String description;
    private String urlPublic;
    private String urlBO;
    private ProductStatus status;

}
