package it.pagopa.selfcare.onboarding.interceptor.web.controller;

import io.swagger.annotations.Api;
import it.pagopa.selfcare.onboarding.interceptor.core.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@Api(tags = "product")
public class InterceptorController {//TODO change Name

    private final NameService nameService;//TODO change Name


    @Autowired
    public InterceptorController(NameService nameService) {
        this.nameService = nameService;
    }

}
