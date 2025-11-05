package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.client.AccountClient;
import com.apj.ecomm.account.web.exception.ActiveOrderExistsException;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.apj.ecomm.account.web.messaging.ShopNameUpdatedEvent;
import com.apj.ecomm.account.web.messaging.ShopStatusUpdatedEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.user")
@RequiredArgsConstructor
class UserService implements IUserService {

	private final UserRepository repository;

	private final AccountClient client;

	private final ApplicationEventPublisher eventPublisher;

	private final PasswordEncoder encoder;

	private final UserMapper mapper;

	@Transactional(readOnly = true)
	public Paged<UserResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toResponseNoIdentifier));
	}

	@Transactional(readOnly = true)
	public UserResponse findByUsername(final String username, final String id) {
		final var user = findBy(username).orElseThrow(ResourceNotFoundException::new);
		if (!id.equals(user.getId().toString()))
			return mapper.toResponseNoIdentifier(user);
		else if (user.isActive())
			return mapper.toResponse(user);
		else
			throw new ResourceNotFoundException();
	}

	public UserResponse update(final String username, final UpdateUserRequest request) {
		validate(request.email(), request.mobileNo());
		return findByActive(username).map(existing -> update(existing, request))
			.orElseThrow(ResourceNotFoundException::new);
	}

	private void validate(final String email, final String mobileNo) {
		repository.findByEmailOrMobileNo(email, mobileNo).ifPresent(user -> {
			final var existing = new HashMap<String, List<String>>();
			if (email != null && email.equals(user.getEmail())) {
				existing.put("email", List.of(email));
			}
			if (mobileNo != null && mobileNo.equals(user.getMobileNo())) {
				existing.put("mobileNo", List.of(mobileNo));
			}
			throw new AlreadyRegisteredException(existing);
		});
	}

	private UserResponse update(final User existing, final UpdateUserRequest request) {
		final var forPublish = forProductDeactivation(existing, request);
		final var shopName = request.shopName();
		final var isShopNameUpdated = shopName != null && !shopName.equals(existing.getShopName());

		final var updated = repository.save(mapper.updateEntity(request, existing, encoder));
		if (forPublish) {
			deactivateProducts(updated);
		}
		else if (updated.getShopName() != null && isShopNameUpdated) {
			final var shopId = mapper.toResponseNoIdentifier(updated).id();
			eventPublisher.publishEvent(new ShopNameUpdatedEvent(shopId, shopName));
		}
		return mapper.toResponse(updated);
	}

	private boolean forProductDeactivation(final User existing, final UpdateUserRequest request) {
		return forProductDeactivation(existing, request.roles() != null && !request.roles().contains(Role.SELLER));
	}

	public void deleteByUsername(final String username) {
		findByActive(username).ifPresentOrElse(this::deactivate, () -> {
			throw new ResourceNotFoundException();
		});
	}

	private void deactivate(final User user) {
		final var forPublish = forProductDeactivation(user);
		user.setActive(false);
		repository.save(user);
		if (forPublish) {
			deactivateProducts(user);
		}
	}

	private boolean forProductDeactivation(final User user) {
		return forProductDeactivation(user, true);
	}

	private boolean forProductDeactivation(final User user, final boolean otherCondition) {
		final var forDeactivation = user.getRoles().contains(Role.SELLER) && otherCondition;
		if (forDeactivation && client.activeOrderExists(user.getId().toString()))
			throw new ActiveOrderExistsException();
		return forDeactivation;
	}

	private void deactivateProducts(final User user) {
		eventPublisher.publishEvent(new ShopStatusUpdatedEvent(user.getId().toString(), Boolean.FALSE));
	}

	private Optional<User> findByActive(final String username) {
		return findBy(username).filter(User::isActive);
	}

	private Optional<User> findBy(final String username) {
		return repository.findByUsername(username);
	}

	public Map<String, UserResponse> findAllBy(final List<UUID> ids) {
		return repository.findAllById(ids)
			.stream()
			.filter(User::isActive)
			.collect(Collectors.toMap(user -> user.getId().toString(), mapper::toResponse));
	}

}
