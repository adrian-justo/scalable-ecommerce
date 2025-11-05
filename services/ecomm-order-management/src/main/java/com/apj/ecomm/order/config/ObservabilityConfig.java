package com.apj.ecomm.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

@Configuration
public class ObservabilityConfig {

	@Bean
	ObservedAspect observedAspect(final ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}

}
