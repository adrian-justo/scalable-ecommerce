package com.apj.ecomm.product.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

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
		final var predicate = getPredicate(root, builder, operation);
		return predicate != null && operation.contains("!") ? predicate.not() : predicate;

	}

	private Predicate getPredicate(final Root<Product> root, final CriteriaBuilder builder, final String operation) {
		final var type = root.get(field).getJavaType();
		final var fieldIsInstant = isInstant(type);

		if (value.contains("->")) {
			final var values = value.split("->");
			return fieldIsInstant ? builder.between(root.get(field), Instant.parse(values[0]), Instant.parse(values[1]))
					: builder.between(root.get(field), values[0], values[1]);
		}
		else if (value.contains(","))
			return lowerIfString(root.get(field), builder).in(valueBy(type, Arrays.stream(value.split(","))).toList());

		return switch (operation.replace("!", "")) {
			case ":" -> Collection.class.isAssignableFrom(type) ? builder.gt(arrayPosition(root, builder), 0)
					: builder.equal(lowerIfString(root.get(field), builder), valueBy(type));
			case "%" -> builder.like(lowerIfString(root.get(field), builder), "%" + value.toLowerCase() + "%");
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

	private Object valueBy(final Class<?> type) {
		if (isInstant(type))
			return Instant.parse(value);
		else if (isBoolean(type))
			return Boolean.valueOf(value);
		else
			return value.toLowerCase();
	}

	private Stream<?> valueBy(final Class<?> type, final Stream<String> values) {
		if (isInstant(type))
			return values.map(Instant::parse);
		else if (isBoolean(type))
			return values.map(Boolean::valueOf);
		else
			return values.map(String::toLowerCase);

	}

	private boolean isInstant(final Class<?> field) {
		return Instant.class.isAssignableFrom(field);
	}

	private boolean isBoolean(final Class<?> field) {
		return Boolean.class.isAssignableFrom(field);
	}

}
