package com.apj.ecomm.account.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
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
	GroupedOpenApi usersApi(@Value("${api.version}") final String apiVersion,
			@Value("${users.path}") final String basePath) {
		final var path = apiVersion + basePath;
		return GroupedOpenApi.builder()
			.group("acctmgt")
			.pathsToMatch(path, path + "/**")
			.addOpenApiCustomizer(pathPrefixRemover(path))
			.build();
	}

	@Bean
	GroupedOpenApi authApi(@Value("${api.version}") final String apiVersion,
			@Value("${auth.path}") final String basePath) {
		final var path = apiVersion + basePath;
		return GroupedOpenApi.builder()
			.group("auth")
			.pathsToMatch(path + "/**")
			.addOpenApiCustomizer(pathPrefixRemover(path))
			.build();
	}

	OpenApiCustomizer pathPrefixRemover(final String path) {
		return openApi -> {
			final var paths = openApi.getPaths();
			final var keySet = paths.keySet().toArray(new String[0]); // To avoid
																		// ConcurrentModificationException

			for (final String key : keySet) {
				paths.put(key.replace(path, ""), paths.get(key));
				paths.remove(key);
			}

			final var server = openApi.getServers().get(0);
			server.setUrl(server.getUrl() + path);
		};
	}

}
