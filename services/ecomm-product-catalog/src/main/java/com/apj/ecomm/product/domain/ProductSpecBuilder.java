package com.apj.ecomm.product.domain;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
class ProductSpecBuilder {

	@Value("${filter.pattern:([;|]*)((\\()(([^()]*|\\([^()]*\\))*)(\\))|(\\w+)(:|!:|%|!%|<|>)([\\w\\s\\/'!:%<>.,+-]*))}")
	private String pattern;

	Specification<Product> build(final String filter) {
		Specification<Product> spec = null;

		if (StringUtils.isNotBlank(filter)) {
			final var matcher = Pattern.compile(pattern).matcher(filter);

			while (matcher.find()) {
				spec = build(matcher.group(1), matcher.group(4), matcher.group(7), matcher.group(8), matcher.group(9),
						spec);
			}
		}
		return spec;
	}

	private Specification<Product> build(final String conjunction, final String group, final String key,
			final String operation, final String value, final Specification<Product> spec) {
		if (StringUtils.isBlank(group)) {
			final var productSpec = new ProductSpec(key, operation, value);
			if (StringUtils.isBlank(conjunction))
				return productSpec;
			else // reversed because AND operations take precedence over OR operations
				return conjunction.equals("|") ? productSpec.or(spec) : spec.and(productSpec);
		}
		else if (StringUtils.isBlank(conjunction))
			return build(group);
		else
			return conjunction.equals("|") ? build(group).or(spec) : spec.and(build(group));
	}

}