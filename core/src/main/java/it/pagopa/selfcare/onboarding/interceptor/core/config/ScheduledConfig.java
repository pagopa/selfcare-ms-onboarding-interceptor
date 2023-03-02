package it.pagopa.selfcare.onboarding.interceptor.core.config;

import it.pagopa.selfcare.onboarding.interceptor.api.InternalApiConnector;
import it.pagopa.selfcare.onboarding.interceptor.api.OnboardingValidationStrategy;
import it.pagopa.selfcare.onboarding.interceptor.core.strategy.OnboardingValidationStrategyCoreImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@PropertySource("classpath:config/core-config.properties")
public class ScheduledConfig implements SchedulingConfigurer {

    @Value("${scheduler.threads.max-number}")
    private int maxScheduleThreadNumber;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(maxScheduleThreadNumber);
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    @Bean
    OnboardingValidationStrategy onboardingValidationStrategyCore(InternalApiConnector apiConnector) {
        return new OnboardingValidationStrategyCoreImpl(apiConnector);
    }
}
