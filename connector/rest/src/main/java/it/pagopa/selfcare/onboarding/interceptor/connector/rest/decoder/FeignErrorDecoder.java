package it.pagopa.selfcare.onboarding.interceptor.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.interceptor.exception.InstitutionAlreadyOnboardedException;

public class FeignErrorDecoder extends ErrorDecoder.Default {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            throw new InstitutionAlreadyOnboardedException();
        } else {
            return super.decode(methodKey, response);
        }
    }

}
