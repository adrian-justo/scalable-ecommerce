package com.apj.ecomm.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

	@Bean
	MongoCustomConversions customConversions() {
		return MongoCustomConversions.create(config -> config.registerConverter(new MessageConverter()));
	}

}
