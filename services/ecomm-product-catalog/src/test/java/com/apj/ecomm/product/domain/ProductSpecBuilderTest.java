package com.apj.ecomm.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ProductSpecBuilderTest {

	private final String field = "any";

	@Mock
	private Root<Product> root;

	@Mock
	private CriteriaQuery<?> query;

	@Mock
	private CriteriaBuilder builder;

	@Mock
	private Path path;

	@Mock
	private Expression expression;

	@Mock
	private Predicate predicate;

	@Mock
	private Predicate or;

	@Mock
	private Predicate and;

	@InjectMocks
	private ProductSpecBuilder specBuilder;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(specBuilder, "pattern",
				"([;|]*)((\\()(([^()]*|\\([^()]*\\))*)(\\))|(\\w+)(:|!:|%|!%|<|>)([\\w\\s\\/'!:%<>.,+-]*))");
		when(root.get(anyString())).thenReturn(path);
	}

	@Test
	void between() {
		final var operation = ":";
		var value = "1->2";

		when(path.getJavaType()).thenReturn(Integer.class);
		when(builder.between(any(Path.class), anyString(), anyString())).thenReturn(predicate);

		assertEquals(builder.between(path, "1", "2"),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		value = "2025-01-01T00:00:00Z->2025-01-01T23:59:59Z";

		when(path.getJavaType()).thenReturn(Instant.class);
		when(builder.between(any(Path.class), any(Instant.class), any(Instant.class))).thenReturn(predicate);

		assertEquals(
				builder.between(path, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T23:59:59Z")),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void equal() {
		final var operation = ":";
		var value = "aNy".toLowerCase();

		when(path.getJavaType()).thenReturn(Set.class);
		when(builder.gt(any(Expression.class), anyInt())).thenReturn(predicate);
		when(builder.function(anyString(), any(Class.class), any(Expression.class), any())).thenReturn(expression);

		assertEquals(builder.gt(expression, 0),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		when(path.getJavaType()).thenReturn(String.class);
		when(builder.lower(any(Path.class))).thenReturn(expression);
		when(builder.equal(any(Expression.class), anyString())).thenReturn(predicate);

		assertEquals(builder.equal(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		value = "2025-01-01T23:59:59Z";

		when(path.getJavaType()).thenReturn(Instant.class);
		when(builder.equal(any(Path.class), any(Instant.class))).thenReturn(predicate);

		assertEquals(builder.equal(path, Instant.parse(value)),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void notEqual() {
		final var operation = "!:";
		var value = "aNy".toLowerCase();

		when(path.getJavaType()).thenReturn(Set.class);
		when(builder.equal(any(Expression.class), anyInt())).thenReturn(predicate);
		when(builder.function(anyString(), any(Class.class), any(Expression.class), any())).thenReturn(expression);

		assertEquals(builder.equal(expression, 0),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		when(path.getJavaType()).thenReturn(String.class);
		when(builder.lower(any(Path.class))).thenReturn(expression);
		when(builder.notEqual(any(Expression.class), anyString())).thenReturn(predicate);

		assertEquals(builder.notEqual(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		value = "2025-01-01T23:59:59Z";

		when(path.getJavaType()).thenReturn(Instant.class);
		when(builder.notEqual(any(Path.class), any(Instant.class))).thenReturn(predicate);

		assertEquals(builder.notEqual(path, Instant.parse(value)),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void like() {
		final var operation = "%";
		final var value = "aNy".toLowerCase();

		when(path.getJavaType()).thenReturn(String.class);
		when(builder.lower(any(Path.class))).thenReturn(expression);
		when(builder.like(any(Expression.class), anyString())).thenReturn(predicate);

		assertEquals(builder.like(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void notLike() {
		final var operation = "!%";
		final var value = "aNy".toLowerCase();

		when(path.getJavaType()).thenReturn(String.class);
		when(builder.lower(any(Path.class))).thenReturn(expression);
		when(builder.notLike(any(Expression.class), anyString())).thenReturn(predicate);

		assertEquals(builder.notLike(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void lessThanOrEqualTo() {
		final var operation = "<";
		var value = "1";

		when(path.getJavaType()).thenReturn(Integer.class);
		when(builder.lessThanOrEqualTo(any(Path.class), anyString())).thenReturn(predicate);

		assertEquals(builder.lessThanOrEqualTo(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		value = "2025-01-01T23:59:59Z";

		when(path.getJavaType()).thenReturn(Instant.class);
		when(builder.lessThanOrEqualTo(any(Path.class), any(Instant.class))).thenReturn(predicate);

		assertEquals(builder.lessThanOrEqualTo(path, Instant.parse(value)),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void greaterThanOrEqualTo() {
		final var operation = ">";
		var value = "1";

		when(path.getJavaType()).thenReturn(Integer.class);
		when(builder.greaterThanOrEqualTo(any(Path.class), anyString())).thenReturn(predicate);

		assertEquals(builder.greaterThanOrEqualTo(path, value),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));

		value = "2025-01-01T23:59:59Z";

		when(path.getJavaType()).thenReturn(Instant.class);
		when(builder.greaterThanOrEqualTo(any(Path.class), any(Instant.class))).thenReturn(predicate);

		assertEquals(builder.greaterThanOrEqualTo(path, Instant.parse(value)),
				specBuilder.build(field + operation + value).toPredicate(root, query, builder));
	}

	@Test
	void combination() {
		final var operation = ":";
		final var value = "1";

		when(path.getJavaType()).thenReturn(Integer.class);
		when(builder.equal(any(Expression.class), anyString())).thenReturn(predicate);
		when(builder.and(any(Predicate.class), any(Predicate.class))).thenReturn(and);
		when(builder.or(any(Predicate.class), any(Predicate.class))).thenReturn(or);

		assertEquals(
				builder.or(builder.equal(path, value),
						builder.and(builder.equal(path, value), builder.equal(path, value))),
				specBuilder
					.build(field + operation + value + "|(" + field + operation + value + ";" + field + operation
							+ value + ")")
					.toPredicate(root, query, builder));
	}

}
