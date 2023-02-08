package it.pagopa.selfcare.onboarding.interceptor.web.validator;

import it.pagopa.selfcare.commons.web.validator.ControllerResponseValidator;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Validator;

@Aspect
@Component
public class OnboardingInterceptorResponseValidator extends ControllerResponseValidator {

    @Autowired
    public OnboardingInterceptorResponseValidator(Validator validator) {
        super(validator);
    }

    @Override
    @Pointcut("execution(* it.pagopa.selfcare.onboarding.interceptor.web.controller.*.*(..))")
    public void controllersPointcut() {
        // Do nothing because is a pointcut
    }

}
