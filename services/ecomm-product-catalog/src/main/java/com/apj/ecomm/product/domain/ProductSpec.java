package com.apj.ecomm.product.domain;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class ProductSpec implements Specification<Product> {

	private static final long serialVersionUID = 9183390246352642790L;

	private String key;
	private String operation;
	private String value;

	@Override
	public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		boolean keyIsInstant = Instant.class.isAssignableFrom(root.get(key).getJavaType());
		boolean keyIsCollection = Collection.class.isAssignableFrom(root.get(key).getJavaType());

		if (value.contains("-")) {
			String[] values = value.split("-");
			return keyIsInstant ? builder.between(root.get(key), parse(values[0]), parse(values[1]))
					: builder.between(root.get(key), values[0], values[1]);
		}

		return switch (operation) {
		case ":" -> keyIsCollection ? builder.gt(arrayPosition(root, builder), 0) : builder.equal(root.get(key), value);
		case "!:" ->
			keyIsCollection ? builder.equal(arrayPosition(root, builder), 0) : builder.notEqual(root.get(key), value);
		case "%" -> builder.like(root.get(key), "%" + value + "%");
		case "!%" -> builder.notLike(root.get(key), "%" + value + "%");
		case "<" -> keyIsInstant ? builder.lessThanOrEqualTo(root.get(key), parse(value))
				: builder.lessThanOrEqualTo(root.get(key), value);
		case ">" -> keyIsInstant ? builder.greaterThanOrEqualTo(root.get(key), parse(value))
				: builder.greaterThanOrEqualTo(root.get(key), value);
		default -> null;
		};
	}

	private Instant parse(String value) {
		return Instant.parse(value.replace("/", "-"));
	}

	private Expression<Integer> arrayPosition(Root<Product> root, CriteriaBuilder builder) {
		return builder.function("array_position", Integer.class, root.get(key), builder.literal(value));
	}

}
