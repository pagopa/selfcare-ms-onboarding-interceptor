package it.pagopa.selfcare.onboarding.interceptor.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.onboarding.interceptor.exception.ResourceNotFoundException;

public class FeignErrorDecoder extends ErrorDecoder.Default {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 409) {
            throw new InstitutionAlreadyOnboardedException();
        } else if (response.status() == 404) {
            throw new ResourceNotFoundException();
        } else {
            return super.decode(methodKey, response);
        }
    }

}
