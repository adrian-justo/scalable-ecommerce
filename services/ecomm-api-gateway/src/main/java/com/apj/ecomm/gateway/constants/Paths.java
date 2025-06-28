package com.apj.ecomm.gateway.constants;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paths")
public record Paths(List<String> permitted, List<String> adminOnly, List<String> buyerOnly, List<String> sellerOnly,
		List<String> nonSeller, List<String> nonBuyer, List<String> userBased) {
}
