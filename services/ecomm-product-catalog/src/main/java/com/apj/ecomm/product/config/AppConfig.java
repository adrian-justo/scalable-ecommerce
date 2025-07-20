package com.apj.ecomm.product.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class AppConfig {

	private final ObjectMapper mapper;

	@Bean
	ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}

	@Bean
	RedisCacheManager redisCacheManager(RedisConnectionFactory conn) {
		return RedisCacheManager.builder(conn).cacheDefaults(configWithTtl(5)).withInitialCacheConfigurations(
				Map.of("catalog", configWithTtlFor(new TypeReference<List<ProductCatalog>>() {
				}, 10), "product", configWithTtlFor(ProductResponse.class, 30))).build();
	}

	private RedisCacheConfiguration configWithTtl(long minutes) {
		return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(minutes));
	}

	private RedisCacheConfiguration configWithTtlFor(TypeReference<?> type, long minutes) {
		return configWithTtl(minutes).disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
						new Jackson2JsonRedisSerializer<>(mapper.getTypeFactory().constructType(type))));
	}

	private RedisCacheConfiguration configWithTtlFor(Class<?> clazz, long minutes) {
		return configWithTtl(minutes).disableCachingNullValues().serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(clazz)));
	}

}
