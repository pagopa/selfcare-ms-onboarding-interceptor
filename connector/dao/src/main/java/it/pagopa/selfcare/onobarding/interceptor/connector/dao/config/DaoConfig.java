package it.pagopa.selfcare.onobarding.interceptor.connector.dao.config;

import it.pagopa.selfcare.onobarding.interceptor.connector.dao.auditing.SpringSecurityAuditorAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(modifyOnCreate = false)
@PropertySource("classpath:config/dao-config.properties")
@Slf4j
class DaoConfig {

    public DaoConfig() {
        log.trace("Initializing {}...", DaoConfig.class.getSimpleName());
    }

    @Bean
    public AuditorAware<String> myAuditorProvider() {
        return new SpringSecurityAuditorAware();
    }

}