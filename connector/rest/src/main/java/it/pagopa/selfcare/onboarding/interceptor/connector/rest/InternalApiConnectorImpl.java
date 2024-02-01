package it.pagopa.selfcare.onboarding.interceptor.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.client.InternalApiRestClient;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.UserResponse;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.onboarding.interceptor.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.AutoApprovalOnboardingRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.Institution;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.OnboardingProductRequest;
import it.pagopa.selfcare.onboarding.interceptor.model.institution.User;
import it.pagopa.selfcare.onboarding.interceptor.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InternalApiConnectorImpl implements InternalApiConnector {

    protected static final String EXTERNAL_ID_IS_REQUIRED = "An institution externalId is required";
    protected static final String PRODUCT_ID_IS_REQUIRED = "A productId is required";
    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required";
    private final InternalApiRestClient restClient;

    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;
    @Autowired
    public InternalApiConnectorImpl(InternalApiRestClient restClient, InstitutionMapper institutionMapper, UserMapper userMapper) {
        this.restClient = restClient;
        this.institutionMapper = institutionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void autoApprovalOnboarding(String externalInstitutionId, String productId, AutoApprovalOnboardingRequest request) {
        log.trace("autoApprovalOnboarding start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER,"autoApprovalOnboarding externalId = {}, productId = {}, request = {}", externalInstitutionId, productId, request);
        Assert.hasText(externalInstitutionId, EXTERNAL_ID_IS_REQUIRED);
        Assert.hasText(productId, PRODUCT_ID_IS_REQUIRED);
        restClient.autoApprovalOnboarding(externalInstitutionId, productId, request);
        log.trace("autoApprovalOnboarding end");
    }

    @Override
    public Institution getInstitutionById(String institutionId) {
        log.trace("getInstitutionById start");
        log.debug("getInstitutionById institutionId = {}", institutionId);
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        InstitutionResponse response = restClient.getInstitutionById(institutionId);
        Institution result = institutionMapper.toInstitution(response);
        log.debug("getInstitutionById result = {}", result);
        log.trace("getInstitutionById end");
        return result;
    }

    @Override
    public List<User> getInstitutionProductUsers(String institutionId, String productId) {
        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}", institutionId, productId);
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.hasText(productId, PRODUCT_ID_IS_REQUIRED);
        List<UserResponse> userResponse = restClient.getInstitutionProductUsers(institutionId, productId);
        List<User> user = userResponse.stream().map(userMapper::toUser).collect(Collectors.toList());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionProductUsers user = {}", user);
        log.trace("getInstitutionProductUsers end");
        return user;
    }

    @Override
    public Product getProduct(String productId) {
        log.trace("getProduct start");
        log.debug("getProduct productId = {}", productId);
        Product product = restClient.getProduct(productId);
        log.debug("getProduct product = {}", product);
        log.trace("getProduct end");
        return product;
    }

    @Override
    public void onboarding(OnboardingProductRequest request) {
        log.trace("onbaording start");
        restClient.onboarding(request);
        log.trace("onbaording end");
    }


}
