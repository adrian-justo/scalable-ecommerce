package com.apj.ecomm.product.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ProductSpecBuilder {

	@Value("${filter.pattern:([;|]*)((\\()(([^()]*|\\([^()]*\\))*)(\\))|(\\w+)(:|!:|%|!%|<|>)([\\w\\s\\/'!:%<>.,+-]*))}")
	private String pattern;

	public Specification<Product> build(String filter) {
		Specification<Product> spec = null;

		if (StringUtils.isNotBlank(filter)) {
			Matcher matcher = Pattern.compile(pattern).matcher(filter);

			while (matcher.find()) {
				spec = build(matcher.group(1), matcher.group(4), matcher.group(7), matcher.group(8), matcher.group(9),
						spec);
			}
		}
		return spec;
	}

	private Specification<Product> build(String conjunction, String group, String key, String operation, String value,
			Specification<Product> spec) {
		if (StringUtils.isBlank(group)) {
			ProductSpec productSpec = new ProductSpec(key, operation, value);
			if (StringUtils.isBlank(conjunction)) {
				return productSpec;
			} else {
				return conjunction.equals("|") ? productSpec.or(spec) : spec.and(productSpec); // reversed because of
																								// and precedence over
																								// or
			}

		} else if (StringUtils.isBlank(conjunction)) {
			return build(group);

		} else {
			return conjunction.equals("|") ? build(group).or(spec) : spec.and(build(group));
		}
	}

}