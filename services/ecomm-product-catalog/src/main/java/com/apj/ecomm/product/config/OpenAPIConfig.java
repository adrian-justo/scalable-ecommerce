package com.apj.ecomm.product.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

	@Bean
	OpenAPI openAPI(@Value("${spring.application.name}") final String title,
			@Value("${spring.application.version}") final String version,
			@Value("${application.description}") final String description,
			@Value("${gateway.url}") final String gatewayUrl) {
		return new OpenAPI().info(new Info().title(title).version(version).description(description))
			.servers(List.of(new Server().url(gatewayUrl)));
	}

	@Bean
	OpenApiCustomizer pathPrefixRemover(@Value("${api.version}") final String apiVersion,
			@Value("${products.path}") final String basePath) {
		return openApi -> {
			final var paths = openApi.getPaths();
			// To avoid ConcurrentModificationException
			final var keySet = paths.keySet().toArray(new String[0]);
			final var path = apiVersion + basePath;

			for (final String key : keySet) {
				paths.put(key.replace(path, ""), paths.get(key));
				paths.remove(key);
			}

			final var server = openApi.getServers().getFirst();
			server.setUrl(server.getUrl() + path);
		};
	}

}
