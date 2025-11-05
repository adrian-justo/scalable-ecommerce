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
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.redis.util.RedisLockRegistry.RedisLockType;

import com.apj.ecomm.product.domain.model.ProductResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

	private final ObjectMapper mapper;

	@Bean
	RedisCacheManager redisCacheManager(final RedisConnectionFactory conn) {
		return RedisCacheManager.builder(conn)
			.cacheDefaults(configWithTtl(10))
			.withInitialCacheConfigurations(
					Map.of("catalog", configWithTtl(5, serializerFor(new TypeReference<List<Long>>() {
					})), "product", configWithTtl(30, serializerFor(ProductResponse.class))))
			.build();
	}

	private RedisCacheConfiguration configWithTtl(final long minutes, final Jackson2JsonRedisSerializer<?> serializer) {
		return configWithTtl(minutes).disableCachingNullValues()
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
	}

	private RedisCacheConfiguration configWithTtl(final long minutes) {
		return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(minutes));
	}

	private Jackson2JsonRedisSerializer<?> serializerFor(final TypeReference<?> type) {
		return new Jackson2JsonRedisSerializer<>(mapper.getTypeFactory().constructType(type));
	}

	private Jackson2JsonRedisSerializer<?> serializerFor(final Class<?> clazz) {
		return new Jackson2JsonRedisSerializer<>(clazz);
	}

	@Bean
	RedisLockRegistry redisLockRegistry(final RedisConnectionFactory conn) {
		final var lockRegistry = new RedisLockRegistry(conn, "product-locks");
		lockRegistry.setRedisLockType(RedisLockType.PUB_SUB_LOCK);
		return lockRegistry;
	}

}
