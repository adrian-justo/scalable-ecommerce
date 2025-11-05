package com.apj.ecomm.product.web.util;

import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.util.CheckedCallable;
import org.springframework.integration.util.CheckedRunnable;
import org.springframework.stereotype.Component;

import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class Executor {

	private static final String LOCK_KEY = "productId:";

	private final RedisLockRegistry lockRegistry;

	public void lockFor(final Long id, final CheckedRunnable<RuntimeException> runnable) {
		try {
			lockRegistry.executeLocked(LOCK_KEY + id, runnable);
		}
		catch (final InterruptedException e) {
			handle(e, id);
		}
	}

	public ProductResponse lockFor(final Long id, final CheckedCallable<ProductResponse, RuntimeException> callable) {
		try {
			return lockRegistry.executeLocked(LOCK_KEY + id, callable);
		}
		catch (final InterruptedException e) {
			handle(e, id);
		}
		catch (final ResourceAccessDeniedException e) {
			throw e;
		}
		throw new ResourceNotFoundException();
	}

	private void handle(final InterruptedException e, final Long id) {
		Thread.currentThread().interrupt();
		log.error("Thread interrupted for id: {}", id, e);
	}

}
