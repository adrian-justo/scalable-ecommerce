package com.apj.ecomm.product.domain;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class ProductSpec implements Specification<Product> {

	private static final long serialVersionUID = 9183390246352642790L;

	private final String field;

	private final String operation;

	private final String value;

	@Override
	public Predicate toPredicate(final Root<Product> root, final CriteriaQuery<?> query,
			final CriteriaBuilder builder) {
		final var fieldIsInstant = Instant.class.isAssignableFrom(root.get(field).getJavaType());
		if (value.contains("->")) {
			final var values = value.split("->");
			return fieldIsInstant ? builder.between(root.get(field), Instant.parse(values[0]), Instant.parse(values[1]))
					: builder.between(root.get(field), values[0].toLowerCase(), values[1].toLowerCase());
		}

		final var fieldIsCollection = Collection.class.isAssignableFrom(root.get(field).getJavaType());
		return switch (operation) {
			case ":" -> fieldIsCollection ? builder.gt(arrayPosition(root, builder), 0)
					: builder.equal(lowerIfString(root.get(field), builder), parseIf(fieldIsInstant, value));
			case "!:" -> fieldIsCollection ? builder.equal(arrayPosition(root, builder), 0)
					: builder.notEqual(lowerIfString(root.get(field), builder), parseIf(fieldIsInstant, value));
			case "%" -> builder.like(lowerIfString(root.get(field), builder), "%" + value.toLowerCase() + "%");
			case "!%" -> builder.notLike(lowerIfString(root.get(field), builder), "%" + value.toLowerCase() + "%");
			case "<" -> fieldIsInstant ? builder.lessThanOrEqualTo(root.get(field), Instant.parse(value))
					: builder.lessThanOrEqualTo(root.get(field), value);
			case ">" -> fieldIsInstant ? builder.greaterThanOrEqualTo(root.get(field), Instant.parse(value))
					: builder.greaterThanOrEqualTo(root.get(field), value);
			default -> null;
		};
	}

	private Expression<Integer> arrayPosition(final Root<Product> root, final CriteriaBuilder builder) {
		return builder.function("array_position", Integer.class, root.get(field), builder.literal(value.toLowerCase()));
	}

	private Expression<String> lowerIfString(final Path<String> path, final CriteriaBuilder builder) {
		return String.class.isAssignableFrom(path.getJavaType()) ? builder.lower(path) : path;
	}

	private Object parseIf(final boolean fieldIsInstant, final String value) {
		return fieldIsInstant ? Instant.parse(value) : value.toLowerCase();
	}

}
