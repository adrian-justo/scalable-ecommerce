package com.apj.ecomm.product.domain.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;

public record Paged<T>(List<T> result, int page, int size, int totalPages, List<Map<String, Object>> sort,
		long totalElements) {

	public void setSort(final Sort criteria) {
		for (final Sort.Order order : criteria) {
			sort.add(Map.of("property", order.getProperty(), "direction", order.getDirection()));
		}
	}

}
